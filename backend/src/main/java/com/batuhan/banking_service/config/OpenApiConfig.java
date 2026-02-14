package com.batuhan.banking_service.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()
                .info(new Info()
                        .title("Batuhan Banking Service API")
                        .version("1.0")
                        .contact(new Contact()
                                .name("Batuhan Baysal")
                                .url("https://github.com/BatuhanBaysal")
                                .email("batuhanbaysal3@gmail.com"))
                        .description("Enterprise-grade banking core engine featuring secure fund transfers, " +
                                "multi-currency account management, and automated financial reporting."))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Local Backend Server"),
                        new Server().url("http://localhost:8081").description("Keycloak Auth Server (Direct)")
                ))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName, new SecurityScheme()
                                .name(securitySchemeName)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Enter the JWT token you received from Keycloak here. (e.g., Bearer {token})")));
    }
}