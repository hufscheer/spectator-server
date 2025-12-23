package com.sports.server.command.cheertalk.infra;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class KorUnsmileConfig {

    @Value("${korunsmile.token}")
    private String korUnsmileToken;

    @Bean
    public WebClient korUnsmileWebClient() {
        return WebClient.builder()
                .baseUrl("https://router.huggingface.co/hf-inference/models")
                .defaultHeader("Authorization", "Bearer " + korUnsmileToken)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }
}