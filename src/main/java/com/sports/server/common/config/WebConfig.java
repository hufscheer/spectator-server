package com.sports.server.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private static final String ALLOW_ALL_PATH = "/**";
    private static final String ALLOWED_METHODS = "*";
    private static final String FRONTEND_LOCALHOST = "http://localhost:3000";
    private static final String FRONTEND_SERVER = "https://hufstreaming.site";
    private static final String FRONTEND_SERVER_TMP = "https://hufstreaming-client.vercel.app";

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping(ALLOW_ALL_PATH)
                .allowedMethods(ALLOWED_METHODS)
                .allowedOrigins(FRONTEND_LOCALHOST, FRONTEND_SERVER, FRONTEND_SERVER_TMP);
    }

}
