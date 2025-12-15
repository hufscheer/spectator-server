package com.sports.server.command.cheertalk.infra;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class HuggingfaceConfig {

    @Value("${huggingface.token}")
    private String huggingfaceToken;

    @Bean
    public WebClient huggingfaceWebClient() {
        return WebClient.builder()
                .baseUrl("https://router.huggingface.co/hf-inference/models")
                .defaultHeader("Authorization", "Bearer " + huggingfaceToken)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }
}