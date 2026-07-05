package com.example.hotelhub.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "HotelHub API",
                version = "1.0",
                description = "HotelHub - Profesyonel Otel Yönetim ve Rezervasyon API Dokümantasyonu"
        ),
        servers = {
                @Server(url = "http://localhost:8080", description = "Yerel Geliştirme Sunucusu")
        },
        security = @SecurityRequirement(name = "bearerAuth") //isteklere kilit koyar.
)
@SecurityScheme(
        name = "bearerAuth",
        description = "JWT Token'ınızı buraya yapıştırın (Örnek: Bearer <token>)",
        scheme = "bearer",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        in = io.swagger.v3.oas.annotations.enums.SecuritySchemeIn.HEADER
)
public class SwaggerConfig {
    // Bu sınıf sadece Swagger konfigürasyonunu tetiklemek için var.
}