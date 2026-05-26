package com.gymtracker.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${server.port:8080}")
    private int serverPort;

    @Bean
    public OpenAPI gymTrackerOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Gym Tracker & Member Subscription API")
                        .description("""
                                Production-grade REST API for gym member management,
                                workout tracking, and subscription lifecycle control.
                                
                                **Roles:**
                                - `ROLE_MEMBER` — view own workouts and subscription
                                - `ROLE_TRAINER` — assign/manage workout programs
                                - `ROLE_ADMIN` — full system access including subscriptions and members
                                
                                **Auth:** Use `/api/v1/auth/login` to obtain a JWT, then click
                                **Authorize** above and paste: `Bearer <your_token>`
                                """)
                        .version("v1.0")
                        .contact(new Contact()
                                .name("Gym Tracker Engineering")
                                .email("dev@gymtracker.com"))
                        .license(new License().name("MIT")))
                .servers(List.of(
                        new Server().url("http://localhost:" + serverPort).description("Local Development"),
                        new Server().url("https://gym-tracker-api.onrender.com").description("Production (Render)")
                ))
                .addSecurityItem(new SecurityRequirement().addList("Bearer Auth"))
                .components(new Components()
                        .addSecuritySchemes("Bearer Auth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Paste your JWT token (without 'Bearer ' prefix)")));
    }
}
