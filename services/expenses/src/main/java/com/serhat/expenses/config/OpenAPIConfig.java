package com.serhat.expenses.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAPIConfig {
    @Bean
    public OpenAPI expensesServiceAPI(){
        return new OpenAPI()
                .info(new Info().title("Expenses For Card Service API")
                        .description("Rest Api for Credit Card Expenses Service")
                        .version("v0.0.1")
                        .license(new License().name("Apache 2.0")))
                .externalDocs(new ExternalDocumentation().description("Check API Docs")
                        .url("http://localhost:8030/api-docs")
                );
    }
}

