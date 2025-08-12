package com.example.blps.infra.minio.xaresources;

import jakarta.transaction.RollbackException;
import jakarta.transaction.SystemException;
import jakarta.transaction.Transaction;
import jakarta.transaction.TransactionManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.jta.JtaTransactionManager;

@Component
@RequiredArgsConstructor
public class MinioEnlister {
    private final JtaTransactionManager transactionManager;
    private final MinioXATransactionalResource minioTxResource;

    public MinioXAResource enlistMinioXAResource() {
        try {
            TransactionManager tm = transactionManager.getTransactionManager();
            if (tm == null) throw new SystemException("Transaction manager not available");
            Transaction t = tm.getTransaction();
            if (t == null) throw new SystemException("Transaction is not active");
            MinioXAResource minioXa = (MinioXAResource) minioTxResource.getXAResource();
            t.enlistResource(minioXa);
            return minioXa;
        } catch (RollbackException | SystemException e) {
            throw new RuntimeException(e);
        }
    }
}
