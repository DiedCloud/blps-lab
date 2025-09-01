package com.example.blps.config;

import com.atomikos.icatch.config.UserTransactionServiceImp;
import com.example.blps.infra.minio.xaresources.MinioXATransactionalResource;
import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

@Configuration
public class MinioConfig {
    @Value("${minio.accessKey}")
    private String accessKey;
    @Value("${minio.secretKey}")
    private String secretKey;
    @Value("${minio.endpoint}")
    private String endpoint;

    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
    }

    // Minio XA registration
    @Bean(destroyMethod = "shutdownForce")
    @DependsOn("transactionManager") // Для правильного порядка инициализации. Иначе warning-и что transaction manager ещё не инициализирован.
    public UserTransactionServiceImp userTransactionServiceImp() {
        UserTransactionServiceImp uts = new UserTransactionServiceImp();
        uts.init();
        return uts;
    }

    @Bean
    @DependsOn("userTransactionServiceImp")
    public MinioXATransactionalResource minioTransactionalResource(MinioClient minioClient, UserTransactionServiceImp uts) {
        MinioXATransactionalResource res = new MinioXATransactionalResource("MinioXA", minioClient);
        uts.registerResource(res); // Предварительная регистрация ресурса для корректного добавления MinioXAResource в транзакцию
        return res;
    }
}
