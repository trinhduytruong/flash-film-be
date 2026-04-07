package com.flash.film.common.config.openapi;

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
        public OpenAPI openAPI() {
                return new OpenAPI()
                                .info(new Info()
                                                .title("Film BE API")
                                                .description("Backend API for Film application — built with Spring Boot 4")
                                                .version("v1.0.0")
                                                .contact(new Contact()
                                                                .name("Flash Team")
                                                                .email("dev@flash.com")))
                                .servers(List.of(
                                                new Server().url("http://localhost:8080").description("Local"),
                                                new Server().url("https://api.film.com").description("Production")))
                                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                                .components(new Components()
                                                .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                                                .name("bearerAuth")
                                                                .type(SecurityScheme.Type.HTTP)
                                                                .scheme("bearer")
                                                                .bearerFormat("JWT")
                                                                .description("Nhập JWT access token (không cần tiền tố 'Bearer')")));
        }
}
