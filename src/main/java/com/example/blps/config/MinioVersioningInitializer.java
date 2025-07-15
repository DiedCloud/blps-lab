package com.example.blps.config;

import io.minio.MinioClient;
import io.minio.SetBucketVersioningArgs;
import io.minio.GetBucketVersioningArgs;
import io.minio.messages.VersioningConfiguration;
import io.minio.messages.VersioningConfiguration.Status;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MinioVersioningInitializer {

    private final MinioClient minioClient;

    private static final String[] BUCKETS = {"videos", "transcriptions"};

    public MinioVersioningInitializer(MinioClient minioClient) {
        this.minioClient = minioClient;
    }

    @PostConstruct
    public void init() {
        for (String bucket : BUCKETS) {
            try {
                VersioningConfiguration config = minioClient.getBucketVersioning(
                        GetBucketVersioningArgs.builder().bucket(bucket).build()
                );

                if (config.status() == null || !config.status().equals(Status.ENABLED)) {
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
                log.error("Failed to check or enable versioning for bucket '{}'", bucket, e);
            }
        }
    }
}

