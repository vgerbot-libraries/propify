package com.vgerbot.example;

import com.vgerbot.propify.core.Propify;

@Propify(location = "classpath:validation-config.yml")
public class ValidationExample {
//    public static void main(String[] args) {
//        ValidationExamplePropify config = ValidationExamplePropify.getInstance();
//
//        System.out.println("============== Validation Examples ==============\n");
//
//        // Server configuration with validation
//        System.out.println("--- Server Configuration ---");
//        System.out.println("Host: " + config.getServer().getHost());
//        System.out.println("Port: " + config.getServer().getPort());
//        System.out.println("Max Threads: " + config.getServer().getMaxThreads());
//        System.out.println("Connection Timeout: " + config.getServer().getConnectionTimeout());
//
//        // Database connection pool settings
//        System.out.println("\n--- Database Pool Configuration ---");
//        System.out.println("Min Pool Size: " + config.getDatabase().getPool().getMinSize());
//        System.out.println("Max Pool Size: " + config.getDatabase().getPool().getMaxSize());
//        System.out.println("Idle Timeout: " + config.getDatabase().getPool().getIdleTimeout());
//
//        // Cache configuration with duration
//        System.out.println("\n--- Cache Configuration ---");
//        System.out.println("TTL: " + config.getCache().getTtl());
//        System.out.println("Max Size: " + config.getCache().getMaxSize());
//
//        // Rate limiting configuration
//        System.out.println("\n--- Rate Limiting ---");
//        System.out.println("Enabled: " + config.getRateLimiting().isEnabled());
//        System.out.println("Max Requests: " + config.getRateLimiting().getMaxRequests());
//        System.out.println("Time Window: " + config.getRateLimiting().getTimeWindow());
//
//        // Email configuration with URL validation
//        System.out.println("\n--- Email Configuration ---");
//        System.out.println("SMTP URL: " + config.getEmail().getSmtpUrl());
//        System.out.println("From Address: " + config.getEmail().getFromAddress());
//
//        // Security settings
//        System.out.println("\n--- Security Configuration ---");
//        System.out.println("Password Min Length: " + config.getSecurity().getPasswordMinLength());
//        System.out.println("Max Login Attempts: " + config.getSecurity().getMaxLoginAttempts());
//        System.out.println("Allowed Origins: " + String.join(", ", config.getSecurity().getAllowedOrigins()));
//    }
}
