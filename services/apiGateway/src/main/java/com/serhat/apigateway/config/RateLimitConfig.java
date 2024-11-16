package com.serhat.apigateway.config;

import com.serhat.apigateway.component.RateLimitFilter;
import com.serhat.apigateway.service.RateLimitService;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RateLimitConfig {

    @Bean
    public FilterRegistrationBean<RateLimitFilter> rateLimitFilter(){
        FilterRegistrationBean<RateLimitFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new RateLimitFilter(new RateLimitService()));
        registrationBean.addUrlPatterns("/api/v1/*");
        return registrationBean;
    }


}
