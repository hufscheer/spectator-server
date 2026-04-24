package com.sports.server.command.cheertalk.infra;

import com.sports.server.command.cheertalk.application.MaskingClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@ConditionalOnProperty(name = "masking.provider", havingValue = "openrouter")
public class OpenRouterMaskingClient implements MaskingClient {

    private final WebClient openRouterWebClient;
    private final String systemPrompt;
    private final String model;

    private static final int MAX_RETRY = 2;
    private static final Duration RETRY_DELAY = Duration.ofSeconds(2);
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(30);

    public OpenRouterMaskingClient(
            WebClient openRouterWebClient,
            @Value("${openrouter.api.masking-prompt:${gemini.api.prompt}}") String systemPrompt,
            @Value("${openrouter.api.masking-model:${openrouter.api.model:qwen/qwen-2.5-72b-instruct}}") String model
    ) {
        this.openRouterWebClient = openRouterWebClient;
        this.systemPrompt = systemPrompt;
        this.model = model;
    }

    @Override
    public String mask(String content) {
        Map<String, Object> body = Map.of(
                "model", model,
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", content)
                )
        );

        try {
            OpenRouterMaskingResponse response = openRouterWebClient.post()
                    .uri("/chat/completions")
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(OpenRouterMaskingResponse.class)
                    .retryWhen(Retry.fixedDelay(MAX_RETRY, RETRY_DELAY)
                            .filter(OpenRouterMaskingClient::isRetryable)
                            .doBeforeRetry(signal -> log.warn(
                                    "OpenRouter masking retry. attempt={}/{}, cause={}",
                                    signal.totalRetries() + 1, MAX_RETRY + 1,
                                    signal.failure().getClass().getSimpleName()))
                            .onRetryExhaustedThrow((spec, signal) -> signal.failure()))
                    .block(REQUEST_TIMEOUT);

            if (response == null) {
                return content;
            }
            String text = response.getFirstContent();
            return text == null || text.isEmpty() ? content : text;
        } catch (Exception e) {
            log.error("OpenRouter masking failed: {}", e.getMessage());
            return content;
        }
    }

    private static boolean isRetryable(Throwable ex) {
        if (ex instanceof WebClientResponseException wcre) {
            int status = wcre.getStatusCode().value();
            return status == 429 || status == 500 || status == 503;
        }
        return false;
    }
}
