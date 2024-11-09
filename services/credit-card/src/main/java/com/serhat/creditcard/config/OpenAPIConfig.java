package com.serhat.creditcard.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAPIConfig {
    @Bean
    public OpenAPI creditCardServiceAPI(){
        return new OpenAPI()
                .info(new Info().title("Credit Card Service API")
                        .description("Rest Api for Credit Card Service")
                        .version("v0.0.1")
                        .license(new License().name("Apache 2.0")))
                .externalDocs(new ExternalDocumentation().description("Check API Docs")
                        .url("http://localhost:8040/api-docs")
                );
    }
}


