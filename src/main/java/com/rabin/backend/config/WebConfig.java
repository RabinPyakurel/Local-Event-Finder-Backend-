package com.rabin.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC configuration for serving static files and CORS
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * Configure resource handlers to serve uploaded files publicly
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Serve files from uploads directory
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/")
                .setCachePeriod(3600); // Cache for 1 hour

        // Alternative absolute path (if relative doesn't work)
        registry.addResourceHandler("/static/uploads/**")
                .addResourceLocations("file:./uploads/")
                .setCachePeriod(3600);
    }

    /**
     * Configure CORS if needed
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:3000", "http://localhost:5173")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}
