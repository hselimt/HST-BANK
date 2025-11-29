package com.hstbank_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class HstBankApiApplication {
    public static void main(String[] args) {
        // Bootstraps the application, starts embedded Tomcat server
        SpringApplication.run(HstBankApiApplication.class, args);
    }
}