package com.sports.server.command.cheertalk.infra;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class BeomiConfig {

    @Value("${beomi.url}")
    private String beomiUrl;

    @Value("${beomi.api-key}")
    private String beomiApiKey;

    @Bean
    public WebClient beomiWebClient() {
        return WebClient.builder()
                .baseUrl(beomiUrl)
                .defaultHeader("Authorization", "Key " + beomiApiKey)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }
}