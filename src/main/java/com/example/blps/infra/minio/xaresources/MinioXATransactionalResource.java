package com.example.blps.infra.minio.xaresources;

import com.atomikos.datasource.ResourceException;
import com.atomikos.datasource.xa.XATransactionalResource;
import io.minio.MinioClient;

import javax.transaction.xa.XAResource;

public class MinioXATransactionalResource extends XATransactionalResource {
    private final MinioClient minioClient;

    public MinioXATransactionalResource(String resourceName, MinioClient minioClient) {
        super(resourceName);
        this.minioClient = minioClient;
    }

    /**
     * Atomikos вызывает этот метод, когда ему нужен реальный XAResource-объект.
     * @return новый экземпляр MinioXAResource, который реализует javax.transaction.xa.XAResource.
     */
    @Override
    protected XAResource refreshXAConnection() throws ResourceException {
        return new MinioXAResource(minioClient);
    }
}
