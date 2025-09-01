package com.example.blps.infra.minio.xaresources;

import io.minio.MinioClient;
import io.minio.ObjectWriteResponse;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.messages.VersioningConfiguration;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

record UploadedFile(String bucket, String key, String versionId) {
}

@Slf4j
@EqualsAndHashCode
public class MinioXAResource implements XAResource {

    @Getter
    private final ThreadLocal<Xid> currentXid;
    private final Map<Xid, List<UploadedFile>> uncommittedFiles;
    private final MinioClient minioClient;

    public MinioXAResource(MinioClient minioClient) {
        this.currentXid = new ThreadLocal<>();
        this.uncommittedFiles = new ConcurrentHashMap<>();
        this.minioClient = minioClient;
    }

    public void uploadFile(String bucket, String objectKey, InputStream data, long size) throws Exception {
        Xid xid = this.currentXid.get();
        if (xid == null) {
            throw new IllegalStateException("uploadFile() called outside of XA transaction");
        }
        if (!uncommittedFiles.containsKey(xid)) {
            throw new IllegalStateException("uncommited files map uninitialized");
        }
        if (checkVersioningDisabled(bucket)) {
            throw new IllegalStateException("Bucket '" + bucket + "' does not have versioning enabled; cannot rollback;");
        }

        ObjectWriteResponse response = minioClient.putObject(PutObjectArgs.builder()
                .bucket(bucket)
                .object(objectKey)
                .stream(data, size, -1)
                .build());

        uncommittedFiles
                .computeIfAbsent(xid, x -> new ArrayList<>())
                .add(new UploadedFile(bucket, objectKey, response.versionId()));

        log.debug("Uploaded file '{}', version '{}'", objectKey, response.versionId());
    }

    public void removeFile(String bucket, String objectKey) throws Exception {
        Xid xid = this.currentXid.get();
        if (xid == null) {
            throw new IllegalStateException("removeFile() called outside of XA transaction");
        }
        if (!uncommittedFiles.containsKey(xid)) {
            throw new IllegalStateException("uncommited files map uninitialized");
        }
        if (checkVersioningDisabled(bucket)) {
            throw new IllegalStateException("Bucket '" + bucket + "' does not have versioning enabled; cannot rollback;");
        }

        minioClient.removeObject(
                io.minio.RemoveObjectArgs.builder()
                        .bucket(bucket)
                        .object(objectKey)
                        .build()
        );

        // Получить все объекты (с названием с префиксом) для поиска versionId созданного delete-marker'а.
        Iterable<io.minio.Result<io.minio.messages.Item>> results =
                minioClient.listObjects(
                        io.minio.ListObjectsArgs.builder()
                                .bucket(bucket)
                                .prefix(objectKey)
                                .includeVersions(true)
                                .build()
                );

        String deleteMarkerVersionId = null;
        List<io.minio.messages.Item> deleteMarkers = new ArrayList<>();

        // Собрать потенциальные маркеры и выбрать последний
        for (io.minio.Result<io.minio.messages.Item> r : results) {
            io.minio.messages.Item it;
            try {
                it = r.get();
            } catch (Exception e) {
                log.warn("Failed to read listObjects result for {}/{}: ", bucket, objectKey, e);
                continue;
            }
            if (objectKey.equals(it.objectName()) && it.isDeleteMarker()) {
                deleteMarkers.add(it);
                if (it.isLatest()) {
                    deleteMarkerVersionId = it.versionId();
                    break;
                }
            }
        }

        // Если не найден marked-as-latest — выбрать наиболее "свежий" по lastModified
        if (deleteMarkerVersionId == null && !deleteMarkers.isEmpty()) {
            deleteMarkers.sort(Comparator.comparing(io.minio.messages.Item::lastModified).reversed());
            deleteMarkerVersionId = deleteMarkers.get(0).versionId();
        }

        // Не удалось обнаружить versionId — бросить исключение, чтобы обработать транзакцию правильно.
        if (deleteMarkerVersionId == null) {
            throw new IllegalStateException("Failed to determine delete-marker versionId for object '" + objectKey + "' in bucket '" + bucket + "'");
        }

        // Добавить запись о неподтверждённой версии
        uncommittedFiles
                .computeIfAbsent(xid, x -> new ArrayList<>())
                .add(new UploadedFile(bucket, objectKey, deleteMarkerVersionId));

        log.debug("Removed file '{}/{}' -> created delete-marker version '{}'", bucket, objectKey, deleteMarkerVersionId);
    }

    boolean checkVersioningDisabled(String bucket) throws io.minio.errors.MinioException, InvalidKeyException, IOException, NoSuchAlgorithmException {
        var versioning = minioClient.getBucketVersioning(
                io.minio.GetBucketVersioningArgs.builder().bucket(bucket).build());
        return (versioning == null
                || versioning.status() == null
                || !versioning.status().equals(VersioningConfiguration.Status.ENABLED));
    }

    @Override
    public void commit(Xid xid, boolean onePhase) {
        uncommittedFiles.remove(xid);
        this.currentXid.remove();
        log.debug("XA transaction committed for xid '{}'", xid);
    }

    @Override
    public void rollback(Xid xid) {
        for (UploadedFile uf : uncommittedFiles.get(xid)) {
            try {
                minioClient.removeObject(RemoveObjectArgs.builder()
                        .bucket(uf.bucket())
                        .object(uf.key())
                        .versionId(uf.versionId())
                        .build());
                log.debug("Rollback: deleted file '{}/{}' version '{}'", uf.bucket(), uf.key(), uf.versionId());
            } catch (Exception e) {
                log.warn("Failed to delete file during rollback: ", e);
            }
        }
        uncommittedFiles.remove(xid);
        this.currentXid.remove();
        log.debug("XA transaction rolled back for xid '{}'", xid);
    }

    @Override
    public void start(Xid xid, int flags) throws XAException {
        if (uncommittedFiles.containsKey(xid)) {
            if (flags == TMJOIN || flags == TMRESUME) {
                log.debug("Joining/resuming existing transaction for Xid: {}", xid);
                return;
            }
            throw new XAException("Transaction already started for Xid: " + xid);
        }

        if (flags == TMNOFLAGS || flags == TMJOIN || flags == TMRESUME) {
            uncommittedFiles.put(xid, new ArrayList<>());
            this.currentXid.set(xid);
            log.debug("XA transaction started for xid '{}'", xid);
        } else {
            throw new XAException("Unsupported flag in start(): " + flags);
        }
    }

    @Override
    public void end(Xid xid, int flags) throws XAException {
        if (!uncommittedFiles.containsKey(xid)) {
            throw new XAException("Transaction not found in end(): " + xid);
        }

        if (flags != TMSUCCESS && flags != TMSUSPEND && flags != TMFAIL) {
            throw new XAException("Unsupported flag in end(): " + flags);
        }

        log.debug("XA transaction ended for xid '{}' with flag '{}'", xid, flags);
    }

    @Override
    public int prepare(Xid xid) {
        return XAResource.XA_OK;
    }

    @Override
    public void forget(Xid xid) {
        uncommittedFiles.remove(xid);
        this.currentXid.remove();
    }

    @Override
    public Xid[] recover(int flag) {
        return this.uncommittedFiles.keySet().toArray(new Xid[0]);
    }

    @Override
    public boolean isSameRM(XAResource xaResource) {
        if (!(xaResource instanceof MinioXAResource other)) return false;
        return this.equals(other);
    }

    @Override
    public int getTransactionTimeout() {
        return 0;
    }

    @Override
    public boolean setTransactionTimeout(int seconds) {
        return true;
    }
}