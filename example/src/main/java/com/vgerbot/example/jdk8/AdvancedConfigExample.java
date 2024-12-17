package com.vgerbot.example.jdk8;

import com.vgerbot.propify.Propify;

@Propify(location = "classpath:advanced-config.yml", generatedClassName = "AdvancedConfig")
public class AdvancedConfigExample {
    public static void main(String[] args) {
        // Set environment variable for demo
        System.setProperty("REDIS_PORT", "6379");

        AdvancedConfig config = AdvancedConfig.getInstance();

        System.out.println("============== Advanced YAML Configuration Examples ==============");
        
        // Environment variable substitution
        System.out.println("\n--- Environment Variable Substitution ---");
        System.out.println("Redis Port: " + config.getRedis().getPort());
        
        // List handling
        System.out.println("\n--- List Handling ---");
        System.out.println("Allowed Origins:");
        config.getCors().getAllowedOrigins().forEach(origin -> 
            System.out.println("  - " + origin));
            
        // Custom type conversion (Duration)
        System.out.println("\n--- Custom Type Conversion ---");
        System.out.println("Session Timeout: " + config.getSession().getTimeout());
        System.out.println("Cache TTL: " + config.getCache().getTtl());
        
        // Nested configuration
        System.out.println("\n--- Nested Configuration ---");
        System.out.println("AWS Region: " + config.getAws().getRegion());
        System.out.println("S3 Bucket: " + config.getAws().getS3().getBucket());
        System.out.println("S3 Access Key: " + config.getAws().getS3().getAccessKey());
        
        // Map handling
        System.out.println("\n--- Map Configuration ---");
        System.out.println("Feature Flags:");
        AdvancedConfig.Features.Flags flags = config.getFeatures().getFlags();
        System.out.println("  darkMode: " + flags.isDarkMode());
        System.out.println("  beta: " + flags.isBeta());
        System.out.println("  analytics: " + flags.isAnalytics());
        System.out.println("  newUserFlow: " + flags.isNewUserFlow());
        System.out.println("  experimentalApi: " + flags.isExperimentalApi());

        // Array of objects
        System.out.println("\n--- Array of Objects ---");
        System.out.println("Endpoints:");
        config.getEndpoints().stream().forEach((endpoint) -> {
            System.out.println("  Path: " + endpoint.getPath());
            System.out.println("  Method: " + endpoint.getMethod());
            System.out.println("  Roles: " + String.join(", ", endpoint.getRoles()));
            System.out.println();
        });
    }
}
