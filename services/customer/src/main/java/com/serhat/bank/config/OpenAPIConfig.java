package com.serhat.bank.config;



import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAPIConfig {

    @Bean
    public OpenAPI customerServiceAPI(){
        return new OpenAPI()
                .info(new Info().title("Customer Service API")
                        .description("REST API for Customer Service")
                        .version("v0.0.1")
                        .license(new License().name("Apache 2.0")))
                .externalDocs(new ExternalDocumentation().description("Check Customer Service API Docs")
                        .url("http://localhost:8070/api-docs") // Customer service URL
                );
    }
}
