package com.serhat.apigateway.routes;

import org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions;
import org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.*;

import java.net.URI;

import static org.springframework.cloud.gateway.server.mvc.filter.FilterFunctions.setPath;


@Configuration
public class Routes {

    @Bean
   public RouterFunction<ServerResponse> customerServiceSwaggerRoute(){
        return GatewayRouterFunctions.route("customer-service-swagger")
                .route(RequestPredicates.path("/aggregate/customer-service/v3/api-docs"), HandlerFunctions.http("http://localhost:8070"))
                .filter(setPath("/api-docs"))
                .build();
    }
    @Bean
    public RouterFunction<ServerResponse> accountServiceSwaggerRoute(){
        return GatewayRouterFunctions.route("account-service-swagger")
                .route(RequestPredicates.path("/aggregate/account-service/v3/api-docs"), HandlerFunctions.http("http://localhost:8060"))
                .filter(setPath("/api-docs"))
                .build();
    }
    @Bean
    public RouterFunction<ServerResponse> creditCardServiceSwaggerRoute(){
        return GatewayRouterFunctions.route("credit-card-service-swagger")
                .route(RequestPredicates.path("/aggregate/credit-card-service/v3/api-docs"), HandlerFunctions.http("http://localhost:8040"))
                .filter(setPath("/api-docs"))
                .build();
    }
    @Bean
    public RouterFunction<ServerResponse> transactionsServiceSwaggerRoute(){
        return GatewayRouterFunctions.route("transaction-service-swagger")
                .route(RequestPredicates.path("/aggregate/transactions-service/v3/api-docs"), HandlerFunctions.http("http://localhost:8050"))
                .filter(setPath("/api-docs"))
                .build();
    }
    @Bean
    public RouterFunction<ServerResponse> loanServiceSwaggerRoute(){
        return GatewayRouterFunctions.route("loan-service-swagger")
                .route(RequestPredicates.path("/aggregate/loan-service/v3/api-docs"), HandlerFunctions.http("http://localhost:8020"))
                .filter(setPath("/api-docs"))
                .build();
    }
    @Bean
    public RouterFunction<ServerResponse> expensesServiceSwaggerRoute(){
        return GatewayRouterFunctions.route("expenses-service-swagger")
                .route(RequestPredicates.path("/aggregate/expenses-service/v3/api-docs"), HandlerFunctions.http("http://localhost:8030"))
                .filter(setPath("/api-docs"))
                .build();
    }




}


