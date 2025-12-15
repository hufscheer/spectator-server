package com.sports.server.command.cheertalk.infra;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class ClarifaiConfig {

    @Value("${clarifai.api-key}")
    private String clarifaiApiKey;

    @Bean
    public WebClient clarifaiWebClient() {
        return WebClient.builder()
                .baseUrl("https://api.clarifai.com/v2")
                .defaultHeader("Authorization", "Key " + clarifaiApiKey)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }
}