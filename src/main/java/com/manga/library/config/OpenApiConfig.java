package com.manga.library.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(info = @Info(title = "Manga Library API", version = "v1"))
@SecurityScheme(
        name = "google_oauth",
        type = SecuritySchemeType.OAUTH2,
        flows = @io.swagger.v3.oas.annotations.security.OAuthFlows(
                authorizationCode = @io.swagger.v3.oas.annotations.security.OAuthFlow(
                        authorizationUrl = "https://accounts.google.com/o/oauth2/v2/auth",
                        tokenUrl = "https://oauth2.googleapis.com/token",
                        scopes = {
                                @io.swagger.v3.oas.annotations.security.OAuthScope(name = "email", description = "Access email"),
                                @io.swagger.v3.oas.annotations.security.OAuthScope(name = "profile", description = "Access profile")
                        }
                )
        )
)
public class OpenApiConfig {
}