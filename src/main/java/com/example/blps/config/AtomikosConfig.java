package com.example.blps.config;

import com.atomikos.icatch.jta.UserTransactionImp;
import com.atomikos.icatch.jta.UserTransactionManager;
import com.atomikos.jdbc.AtomikosDataSourceBean;
import jakarta.transaction.SystemException;
import org.postgresql.xa.PGXADataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Primary;
import org.springframework.transaction.jta.JtaTransactionManager;

import javax.sql.DataSource;

@Configuration
public class AtomikosConfig {
    @Value("${spring.datasource.url}")
    private String PG_URL;
    @Value("${spring.datasource.username}")
    private String PGUser;
    @Value("${spring.datasource.password}")
    private String PGPass;
    @Value("${spring.datasource.hikari.schema}")
    private String PSSchema;

    @Bean
    @DependsOn("userTransactionServiceImp")
    public DataSource xaDataSource() {
        PGXADataSource pgXaDataSource = new PGXADataSource();
        pgXaDataSource.setUrl(PG_URL);
        pgXaDataSource.setUser(PGUser);
        pgXaDataSource.setPassword(PGPass);
        pgXaDataSource.setCurrentSchema(PSSchema);

        AtomikosDataSourceBean xaDataSource = new AtomikosDataSourceBean();
        xaDataSource.setUniqueResourceName("PostgresXA");
        xaDataSource.setXaDataSource(pgXaDataSource);
        xaDataSource.setMinPoolSize(1);
        xaDataSource.setMaxPoolSize(10);
        return xaDataSource;
    }

    @Bean
    @Primary
    public JtaTransactionManager transactionManager(UserTransactionManager utm, UserTransactionImp utx) {
        return new JtaTransactionManager(utx, utm);
    }

    @Bean(initMethod = "init", destroyMethod = "close")
    public UserTransactionManager userTransactionManager() throws SystemException {
        UserTransactionManager utm = new UserTransactionManager();
        utm.init();
        return utm;
    }

    @Bean
    public UserTransactionImp userTransactionImp() throws SystemException {
        UserTransactionImp utx = new UserTransactionImp();
        utx.setTransactionTimeout(300);
        return utx;
    }
}
