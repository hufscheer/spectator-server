package com.sports.server.command.nl.infra;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sports.server.command.nl.application.NlClient;
import com.sports.server.command.nl.dto.NlParseResult;
import com.sports.server.common.exception.CustomException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@ConditionalOnProperty(name = "nl.provider", havingValue = "openrouter")
public class NlOpenRouterClient implements NlClient {

    private final WebClient openRouterWebClient;
    private final ObjectMapper objectMapper;
    private final String systemPrompt;
    private final String model;

    private static final int MAX_RETRY = 2;
    private static final Duration RETRY_DELAY = Duration.ofSeconds(2);

    public NlOpenRouterClient(
            WebClient openRouterWebClient,
            ObjectMapper objectMapper,
            @Value("${openrouter.api.nl-prompt:${gemini.api.nl-prompt}}") String systemPrompt,
            @Value("${openrouter.api.model:qwen/qwen-2.5-72b-instruct}") String model
    ) {
        this.openRouterWebClient = openRouterWebClient;
        this.objectMapper = objectMapper;
        this.systemPrompt = systemPrompt;
        this.model = model;
    }

    private static final Map<String, Object> FUNCTION_TOOL = Map.of(
            "type", "function",
            "function", Map.of(
                    "name", "parse_players",
                    "description", "텍스트에서 추출한 선수 정보 목록",
                    "parameters", Map.of(
                            "type", "object",
                            "properties", Map.of(
                                    "players", Map.of(
                                            "type", "array",
                                            "items", Map.of(
                                                    "type", "object",
                                                    "properties", Map.of(
                                                            "name", Map.of("type", "string", "description", "선수 이름"),
                                                            "studentNumber", Map.of("type", "string", "description", "9자리 또는 10자리 학번"),
                                                            "jerseyNumber", Map.of("type", "integer", "description", "등번호 (1~99)")
                                                    ),
                                                    "required", List.of("name", "studentNumber")
                                            )
                                    )
                            ),
                            "required", List.of("players")
                    )
            )
    );

    @Override
    public NlParseResult parsePlayers(String message, List<Map<String, String>> history, int studentNumberDigits) {
        OpenRouterChatResponse response = callWithRetry(message, history, studentNumberDigits);
        return toParseResult(response);
    }

    private OpenRouterChatResponse callWithRetry(String message, List<Map<String, String>> history, int studentNumberDigits) {
        Map<String, Object> body = buildRequestBody(message, history, studentNumberDigits);

        try {
            return openRouterWebClient.post()
                    .uri("/chat/completions")
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(OpenRouterChatResponse.class)
                    .retryWhen(Retry.fixedDelay(MAX_RETRY, RETRY_DELAY)
                            .filter(NlOpenRouterClient::isRetryable)
                            .doBeforeRetry(signal -> log.warn(
                                    "OpenRouter retry. attempt={}/{}, cause={}",
                                    signal.totalRetries() + 1, MAX_RETRY + 1,
                                    signal.failure().getClass().getSimpleName()))
                            .onRetryExhaustedThrow((spec, signal) -> signal.failure()))
                    .block(Duration.ofSeconds(60));
        } catch (WebClientResponseException | IllegalStateException e) {
            log.error("OpenRouter call failed after retries: {}", e.getMessage());
            throw new CustomException(HttpStatus.SERVICE_UNAVAILABLE,
                    "AI 서비스가 일시적으로 응답하지 않습니다. 잠시 후 다시 시도해주세요.");
        }
    }

    private static boolean isRetryable(Throwable ex) {
        if (ex instanceof WebClientResponseException wcre) {
            int status = wcre.getStatusCode().value();
            return status == 429 || status == 500 || status == 503;
        }
        return false;
    }

    private Map<String, Object> buildRequestBody(String message, List<Map<String, String>> history, int studentNumberDigits) {
        String perCallInstruction = String.format(
                "이 요청의 학번 자릿수는 정확히 %d자리다. %d자리가 아닌 숫자는 학번으로 추출하지 마.",
                studentNumberDigits, studentNumberDigits
        );
        List<Map<String, Object>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", systemPrompt + "\n\n" + perCallInstruction));
        if (history != null) {
            for (Map<String, String> entry : history) {
                String role = entry.get("role");
                String content = entry.get("content");
                if ("user".equals(role) && content != null) {
                    messages.add(Map.of("role", "user", "content", content));
                }
            }
        }
        messages.add(Map.of("role", "user", "content", message));

        return Map.of(
                "model", model,
                "messages", messages,
                "tools", List.of(FUNCTION_TOOL),
                "tool_choice", Map.of(
                        "type", "function",
                        "function", Map.of("name", "parse_players")
                )
        );
    }

    private NlParseResult toParseResult(OpenRouterChatResponse response) {
        if (response == null || !response.hasToolCall()) {
            String text = response == null ? null : response.getText();
            return NlParseResult.ofText(text == null || text.isEmpty() ? null : text);
        }

        NlFunctionCallArgs args = response.getArgsAs(objectMapper, NlFunctionCallArgs.class);
        if (args == null || args.players() == null || args.players().isEmpty()) {
            return NlParseResult.ofPlayers(List.of());
        }
        return NlParseResult.ofPlayers(args.players());
    }

}
