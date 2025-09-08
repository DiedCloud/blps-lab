package com.example.blps.config;

import com.rabbitmq.jms.admin.RMQConnectionFactory;
import jakarta.jms.ConnectionFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.jms.core.JmsTemplate;

@Configuration
@EnableConfigurationProperties(RabbitJmsProperties.class)
@RequiredArgsConstructor
@EnableJms
public class RabbitJmsPublisherConfig {

    private final RabbitJmsProperties props;

    /**
     * Нативный ConnectionFactory от RabbitMQ JMS client
     */
    @Bean
    public ConnectionFactory nativeRmqConnectionFactory() {
        RMQConnectionFactory rmq = new RMQConnectionFactory();

        // В зависимости от версии клиента можно использовать setHost/setPort или setUri(...)
        rmq.setHost(props.getHost());
        rmq.setPort(props.getPort());
        rmq.setUsername(props.getUsername());
        rmq.setPassword(props.getPassword());
        rmq.setVirtualHost(props.getVirtualHost());

        return rmq;
    }

    @Bean
    public CachingConnectionFactory connectionFactory(ConnectionFactory nativeRmqConnectionFactory) {
        CachingConnectionFactory caching = new CachingConnectionFactory(nativeRmqConnectionFactory);
        caching.setSessionCacheSize(10);
        return caching;
    }

    @Bean
    public JmsTemplate jmsTemplate(ConnectionFactory connectionFactory) {
        return new JmsTemplate(connectionFactory);
    }
}
