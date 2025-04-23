package com.example.blps.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @io.swagger.v3.oas.annotations.info.Info(
                title = "blps lab API",
                description = "backend приложение, лабораторная работа ИТМО",
                version = "1.0.0",
                contact = @Contact(
                        name = "DiedCloud",
                        email = "frolovkirill@niuitmo.ru"
                )
        ),
        servers = {
                @Server(url = "http://localhost:8080", description = "Default Server URL")
        }
)
@SecurityScheme(
        name = "Bearer Authentication",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        scheme = "bearer"
)
public class OpenApiConfiguration {
}