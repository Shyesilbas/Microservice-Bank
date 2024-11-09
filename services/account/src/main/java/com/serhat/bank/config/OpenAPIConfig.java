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
    public OpenAPI accountServiceAPI(){
        return new OpenAPI()
                .info(new Info().title("Account Service API")
                        .description("Rest Api for Account Service")
                        .version("v0.0.1")
                        .license(new License().name("Apache 2.0")))
                .externalDocs(new ExternalDocumentation().description("Check API Docs")
                        .url("http://localhost:8060/api-docs")
                );
    }


}


