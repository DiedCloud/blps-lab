package com.example.blps.config;

import com.example.blps.jca.HiveClient;
import com.example.blps.jca.HiveConnectionFactory;
import com.example.blps.jca.HiveManagedConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConnectorConfig {
    @Value("${hive.base-url}") private String baseUrl;
    @Value("${hive.api.banks.path:/api/v3/hive/text-moderation}") private String apiPath;
    @Value("${hive.api.key}") private String apiKey;

    @Bean
    public HiveClient hiveClient(RestTemplateBuilder restTemplateBuilder) {
        return new HiveClient(restTemplateBuilder, baseUrl, apiPath, apiKey);
    }

    @Bean(name = "eis/HiveConnectionFactory")
    public HiveConnectionFactory hiveConnectionFactory(HiveClient hiveClient) {
        var mcf = new HiveManagedConnectionFactory(hiveClient);
        return new HiveConnectionFactory(mcf);
    }

}
