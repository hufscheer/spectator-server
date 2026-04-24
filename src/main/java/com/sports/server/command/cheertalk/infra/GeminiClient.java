package com.sports.server.command.cheertalk.infra;

import com.sports.server.command.cheertalk.application.MaskingClient;
import com.sports.server.command.cheertalk.dto.GeminiResponse;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Component
@ConditionalOnProperty(name = "masking.provider", havingValue = "gemini", matchIfMissing = true)
public class GeminiClient implements MaskingClient {

    private final WebClient geminiWebClient;
    private final String apiKey;
    private final String prompt;

    public GeminiClient(
            WebClient geminiWebClient,
            @Value("${gemini.api.key}") String apiKey,
            @Value("${gemini.api.prompt}") String prompt
    ) {
        this.geminiWebClient = geminiWebClient;
        this.apiKey = apiKey;
        this.prompt = prompt;
    }

    @Override
    public String mask(String content) {
        try {
            String input = String.join("\n", prompt, content);
            GeminiResponse response = getGeminiResponse(input);
            if (response == null) {
                return content;
            }
            String text = response.getFirstText();
            return text == null || text.isEmpty() ? content : text;
        } catch (Exception e) {
            log.error("Gemini masking failed: {}", e.getMessage());
            return content;
        }
    }

    public GeminiResponse getGeminiResponse(String prompt) {
        Map<String, Object> body = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(
                                Map.of("text", prompt)
                        ))
                )
        );

        return geminiWebClient.post()
                .header("x-goog-api-key", apiKey)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(GeminiResponse.class)
                .block();
    }
}
