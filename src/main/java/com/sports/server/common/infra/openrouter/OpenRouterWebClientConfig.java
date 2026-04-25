package com.sports.server.common.infra.openrouter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@ConditionalOnExpression(
        "'${nl.provider:}'.equals('openrouter') or '${masking.provider:}'.equals('openrouter')"
)
public class OpenRouterWebClientConfig {

    @Value("${openrouter.api.base-url:https://openrouter.ai/api/v1}")
    private String baseUrl;

    @Value("${openrouter.api.key}")
    private String apiKey;

    @Bean
    public WebClient openRouterWebClient() {
        return WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Bean
    public OpenRouterChatCaller openRouterChatCaller(WebClient openRouterWebClient) {
        return new OpenRouterChatCaller(openRouterWebClient);
    }
}
