package com.example.blps.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "rabbit.jms")
public class RabbitJmsProperties {
    @Value("${spring.rabbitmq.host}")
    private String host = "localhost";
    @Value("${spring.rabbitmq.port}")
    private int port = 5672;
    @Value("${spring.rabbitmq.username}")
    private String username = "guest";
    @Value("${spring.rabbitmq.password}")
    private String password = "guest";
    private String virtualHost = "/";
}
