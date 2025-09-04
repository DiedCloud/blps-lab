package com.example.blps.config;

import io.minio.BucketExistsArgs;
import io.minio.GetBucketVersioningArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.SetBucketVersioningArgs;
import io.minio.messages.VersioningConfiguration;
import io.minio.messages.VersioningConfiguration.Status;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MinioVersioningInitializer {
    private final MinioClient minioClient;

    @Value("${minio.buckets.videos}")
    private String videosBucket;
    @Value("${minio.buckets.transcriptions}")
    private String transcriptionsBucket;

    public MinioVersioningInitializer(MinioClient minioClient) {
        this.minioClient = minioClient;
    }

    @PostConstruct
    public void init() {
        String[] buckets = {videosBucket, transcriptionsBucket};

        for (String bucket : buckets) {
            try {
                createBucketIfNotExistsWithRetry(bucket);
                enableVersioningIfNeeded(bucket);
            } catch (Exception e) {
                String msg = "Failed to ensure bucket '" + bucket + "' is ready — failing startup";
                log.error(msg, e);
                throw new RuntimeException(msg, e);
            }
        }
    }

    private void createBucketIfNotExistsWithRetry(String bucket) throws InterruptedException {
        try {
            boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
            if (exists) {
                log.info("Bucket '{}' already exists", bucket);
                return;
            }

            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
            log.info("Bucket '{}' created", bucket);

        } catch (Exception e) {
            String msg = "Failed to create bucket '" + bucket + "'";
            log.error(msg, e);
            throw new RuntimeException(msg, e);
        }
    }

    private void enableVersioningIfNeeded(String bucket) {
        try {
            VersioningConfiguration config = minioClient.getBucketVersioning(
                    GetBucketVersioningArgs.builder().bucket(bucket).build()
            );

            if (config == null || config.status() == null || !config.status().equals(Status.ENABLED)) {
                minioClient.setBucketVersioning(
                        SetBucketVersioningArgs.builder()
                                .bucket(bucket)
                                .config(new VersioningConfiguration(Status.ENABLED, null))
                                .build()
                );
                log.info("Versioning enabled for bucket '{}'", bucket);
            } else {
                log.info("Versioning already enabled for bucket '{}'", bucket);
            }
        } catch (Exception e) {
            String msg = "Failed to check or enable versioning for bucket '" + bucket + "'";
            log.error(msg, bucket, e);
            throw new RuntimeException(msg, e);
        }
    }
}
