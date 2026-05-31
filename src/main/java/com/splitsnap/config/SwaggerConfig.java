package com.splitsnap.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "Pega aquí el token JWT con formato Bearer"
)
public class SwaggerConfig {

@Bean
public OpenAPI customOpenAPI() {
        return new OpenAPI()
        .info(new Info()
                .title("SplitSnap API")
                .version("1.0.0")
                .description("API REST para gestión de gastos compartidos, grupos, deudas y transacciones.")
                .contact(new Contact()
                        .name("SplitSnap")
                        .email("soporte@splitsnap.com"))
                .license(new License()
                        .name("MIT")
                        .url("https://opensource.org/licenses/MIT")))
        .externalDocs(new ExternalDocumentation()
                .description("Documentación adicional de SplitSnap")
                .url("https://github.com"));
        }
}