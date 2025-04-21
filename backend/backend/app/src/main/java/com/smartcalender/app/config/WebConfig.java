package com.smartcalender.app.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configures web-related settings for the SmartCalender application.
 * This class implements WebMvcConfigurer to customize Spring MVC behavior,
 * specifically focusing on enabling Cross-Origin Resource Sharing (CORS) to allow
 * the frontend application to communicate with the backend API securely.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * Configures CORS mappings to enable cross-origin requests for the API.
     * This method allows the frontend to access
     * the backend’s /api/** endpoints by setting permissive CORS policies.
     * It specifies allowed origins, HTTP methods, and headers to ensure compatibility
     * with typical RESTful API interactions while maintaining flexibility for development
     * and production environments.
     *
     * @param registry the CorsRegistry to configure CORS settings
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("*", "http://localhost:3000")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*");
    }
}