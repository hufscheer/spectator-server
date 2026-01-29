package com.sports.server.command.cheertalk.infra;

import com.sports.server.command.cheertalk.dto.GeminiResponse;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@RequiredArgsConstructor
public class GeminiClient {

    private final WebClient geminiWebClient;

    @Value("${gemini.api.key}")
    private String apiKey;

    public GeminiResponse getGeminiResponse(String prompt) {
        Map<String, Object> body = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(
                                Map.of("text", prompt)
                        ))
                )
        );

        return geminiWebClient.post()
                .header("x-goog-api-key", apiKey) // 헤더로 전달
                .bodyValue(body)
                .retrieve()
                .bodyToMono(GeminiResponse.class)
                .block();
    }
}
