package com.sports.server.command.nl.infra;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sports.server.command.nl.application.NlClient;
import com.sports.server.command.nl.dto.NlParseResult;
import com.sports.server.common.exception.CustomException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
public class NlGeminiClient implements NlClient {

    private final WebClient geminiWebClient;
    private final ObjectMapper objectMapper;
    private final List<String> apiKeys;
    private final String systemPrompt;
    private final AtomicInteger keyIndex = new AtomicInteger(0);

    private static final int MAX_RETRY = 2;
    private static final Duration RETRY_DELAY = Duration.ofSeconds(2);

    public NlGeminiClient(
            WebClient geminiWebClient,
            ObjectMapper objectMapper,
            @Value("${gemini.api.keys:}") String keys,
            @Value("${gemini.api.key:}") String singleKey,
            @Value("${gemini.api.nl-prompt}") String systemPrompt
    ) {
        this.geminiWebClient = geminiWebClient;
        this.objectMapper = objectMapper;
        this.systemPrompt = systemPrompt;
        this.apiKeys = parseApiKeys(keys, singleKey);
    }

    private List<String> parseApiKeys(String keys, String singleKey) {
        List<String> result = new ArrayList<>();
        if (keys != null && !keys.isBlank()) {
            for (String key : keys.split(",")) {
                String trimmed = key.trim();
                if (!trimmed.isEmpty()) {
                    result.add(trimmed);
                }
            }
        }
        if (result.isEmpty() && singleKey != null && !singleKey.isBlank()) {
            result.add(singleKey.trim());
        }
        if (result.isEmpty()) {
            throw new IllegalStateException("Gemini API 키가 설정되지 않았습니다.");
        }
        return result;
    }

    private String getNextApiKey() {
        int index = keyIndex.getAndUpdate(i -> (i + 1) % apiKeys.size());
        return apiKeys.get(index);
    }

    private static final Map<String, Object> FUNCTION_SCHEMA = Map.of(
            "name", "parse_players",
            "description", "텍스트에서 추출한 선수 정보 목록",
            "parameters", Map.of(
                    "type", "OBJECT",
                    "properties", Map.of(
                            "players", Map.of(
                                    "type", "ARRAY",
                                    "items", Map.of(
                                            "type", "OBJECT",
                                            "properties", Map.of(
                                                    "name", Map.of("type", "STRING", "description", "선수 이름"),
                                                    "studentNumber", Map.of("type", "STRING", "description", "9자리 학번"),
                                                    "jerseyNumber", Map.of("type", "INTEGER", "description", "등번호 (1~99)")
                                            ),
                                            "required", List.of("name", "studentNumber")
                                    )
                            )
                    ),
                    "required", List.of("players")
            )
    );

    @Override
    public NlParseResult parsePlayers(String message, List<Map<String, String>> history) {
        GeminiFunctionCallResponse response = callGeminiApiWithRetry(message, history);
        return toParseResult(response);
    }

    private GeminiFunctionCallResponse callGeminiApiWithRetry(String message, List<Map<String, String>> history) {
        List<Map<String, Object>> contents = buildContents(message, history);
        Map<String, Object> body = buildRequestBody(contents);

        for (int attempt = 0; attempt <= MAX_RETRY; attempt++) {
            String apiKey = getNextApiKey();
            try {
                return geminiWebClient.post()
                        .header("x-goog-api-key", apiKey)
                        .bodyValue(body)
                        .retrieve()
                        .bodyToMono(GeminiFunctionCallResponse.class)
                        .block(Duration.ofSeconds(30));
            } catch (WebClientResponseException.TooManyRequests e) {
                log.warn("Gemini API rate limit (429). attempt={}, keyIndex={}", attempt + 1, keyIndex.get());
                if (attempt < MAX_RETRY) {
                    sleep(RETRY_DELAY);
                } else {
                    throw new CustomException(HttpStatus.TOO_MANY_REQUESTS, "AI 서비스가 일시적으로 사용량이 많습니다. 잠시 후 다시 시도해주세요.");
                }
            }
        }
        throw new CustomException(HttpStatus.TOO_MANY_REQUESTS, "AI 서비스가 일시적으로 사용량이 많습니다. 잠시 후 다시 시도해주세요.");
    }

    private Map<String, Object> buildRequestBody(List<Map<String, Object>> contents) {
        return Map.of(
                "systemInstruction", Map.of(
                        "parts", List.of(Map.of("text", systemPrompt))
                ),
                "contents", contents,
                "tools", List.of(Map.of(
                        "functionDeclarations", List.of(FUNCTION_SCHEMA)
                )),
                "toolConfig", Map.of(
                        "functionCallingConfig", Map.of("mode", "ANY")
                )
        );
    }

    private NlParseResult toParseResult(GeminiFunctionCallResponse response) {
        if (!response.hasFunctionCall()) {
            String text = response.getText();
            return NlParseResult.ofText(text.isEmpty() ? null : text);
        }

        GeminiFunctionCallArgs args = response.getArgsAs(objectMapper, GeminiFunctionCallArgs.class);
        if (args == null || args.players() == null || args.players().isEmpty()) {
            return NlParseResult.ofPlayers(List.of());
        }

        return NlParseResult.ofPlayers(args.players());
    }

    private static final Map<String, String> ALLOWED_ROLES = Map.of(
            "user", "user"
    );

    private List<Map<String, Object>> buildContents(String message, List<Map<String, String>> history) {
        List<Map<String, Object>> contents = new ArrayList<>();

        if (history != null) {
            for (Map<String, String> entry : history) {
                String role = entry.get("role");
                String content = entry.get("content");
                String geminiRole = ALLOWED_ROLES.get(role);
                if (geminiRole != null && content != null) {
                    contents.add(Map.of(
                            "role", geminiRole,
                            "parts", List.of(Map.of("text", content))
                    ));
                }
            }
        }

        contents.add(Map.of(
                "role", "user",
                "parts", List.of(Map.of("text", message))
        ));

        return contents;
    }

    private void sleep(Duration duration) {
        try {
            Thread.sleep(duration.toMillis());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
