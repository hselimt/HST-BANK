package com.hstbank_api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AppConfig {

    // RestTemplate = Spring's HTTP client for making API calls
    // @Bean = registers this as a Spring-managed singleton
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
