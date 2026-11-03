package com.QuadGPT.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class OpenApiConfig {

    private static final String BEARER = "bearerAuth";

    // Adds the "Authorize" button to Swagger UI: paste the token returned by /api/auth/login.
    @Bean
    public OpenAPI quadGptOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("QuadGPT API")
                        .version("1.0")
                        .description("""
                                Enterprise RAG chatbot. A USER only gets answers and documents from their \
                                department; an ADMIN sees every document and manages users and uploads.
                                Errors are returned as plain text: 400 invalid input, 401 missing or invalid \
                                token, 403 wrong role or department, 404 not found, 409 already exists, \
                                413 file too large, 503 AI service unavailable."""))
                .components(new Components().addSecuritySchemes(BEARER, new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList(BEARER));
    }
}
