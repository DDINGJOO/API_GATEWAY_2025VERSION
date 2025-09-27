package com.study.api_gateway.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.License;
import org.springframework.context.annotation.Configuration;

@OpenAPIDefinition(
        info = @Info(
                title = "Bander API Gateway",
                version = "v1",
                description = "API Gateway for Bander services. Exposes BFF endpoints.",
                contact = @Contact(name = "Bander Team"),
                license = @License(name = "MIT")
        )
)
@Configuration
public class OpenApiConfig {
    // Additional configuration can be added here if needed.
}
