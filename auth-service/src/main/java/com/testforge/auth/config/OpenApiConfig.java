package com.testforge.auth.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

	@Bean
	public OpenAPI productServiceOpenAPI() {
	    return new OpenAPI()
	            .info(new Info()
	            		.title("TestForge Auth Service API")
                        .description(
                                "APIs for registration, OTP verification, "
                                + "authentication, and logout.")
                        .version("1.0.0"))
	            .components(new Components()
	                    .addSecuritySchemes("bearerAuth",
	                            new SecurityScheme()
	                                    .type(SecurityScheme.Type.HTTP)
	                                    .scheme("bearer")
	                                    .bearerFormat("JWT")));
	}
}