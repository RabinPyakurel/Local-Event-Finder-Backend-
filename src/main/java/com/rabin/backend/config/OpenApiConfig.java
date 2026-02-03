package com.rabin.backend.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Local Event Finder API",
                version = "1.0.0",
                description = "REST API for Local Event Finder - A platform to discover, create, and manage local events",
                contact = @Contact(
                        name = "Rabin",
                        email = "support@localeventfinder.com"
                ),
                license = @License(
                        name = "MIT License",
                        url = "https://opensource.org/licenses/MIT"
                )
        ),
        servers = {
                @Server(url = "http://localhost:8080", description = "Development Server"),
                @Server(url = "https://api.localeventfinder.com", description = "Production Server")
        }
)
@SecurityScheme(
        name = "bearerAuth",
        description = "JWT Bearer token authentication. Enter your token without 'Bearer ' prefix.",
        scheme = "bearer",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER
)
public class OpenApiConfig {
}
