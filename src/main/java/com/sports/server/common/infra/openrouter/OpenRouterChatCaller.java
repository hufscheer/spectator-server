package com.sports.server.common.infra.openrouter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.Map;

@Slf4j
public class OpenRouterChatCaller {

    private static final int MAX_RETRY = 2;
    private static final Duration RETRY_DELAY = Duration.ofSeconds(2);

    private final WebClient openRouterWebClient;

    public OpenRouterChatCaller(WebClient openRouterWebClient) {
        this.openRouterWebClient = openRouterWebClient;
    }

    public OpenRouterChatResponse call(Map<String, Object> body, Duration timeout) {
        return openRouterWebClient.post()
                .uri("/chat/completions")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(OpenRouterChatResponse.class)
                .retryWhen(Retry.fixedDelay(MAX_RETRY, RETRY_DELAY)
                        .filter(OpenRouterChatCaller::isRetryable)
                        .doBeforeRetry(signal -> log.warn(
                                "OpenRouter retry. attempt={}/{}, cause={}",
                                signal.totalRetries() + 1, MAX_RETRY + 1,
                                signal.failure().getClass().getSimpleName()))
                        .onRetryExhaustedThrow((spec, signal) -> signal.failure()))
                .block(timeout);
    }

    private static boolean isRetryable(Throwable ex) {
        if (ex instanceof WebClientResponseException wcre) {
            int status = wcre.getStatusCode().value();
            return status == 429 || status == 500 || status == 503;
        }
        return false;
    }
}
