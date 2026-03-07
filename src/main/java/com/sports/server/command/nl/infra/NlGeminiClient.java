package com.sports.server.command.nl.infra;

import com.sports.server.command.nl.application.NlClient;
import com.sports.server.command.nl.dto.NlParseResult;
import com.sports.server.command.nl.dto.NlParseResult.ParsedPlayer;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class NlGeminiClient implements NlClient {

    private final WebClient geminiWebClient;

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.nl-prompt}")
    private String systemPrompt;

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
        GeminiFunctionCallResponse response = callGeminiApi(message, history);
        return toParseResult(response);
    }

    private GeminiFunctionCallResponse callGeminiApi(String message, List<Map<String, String>> history) {
        List<Map<String, Object>> contents = buildContents(message, history);

        Map<String, Object> body = Map.of(
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

        return geminiWebClient.post()
                .header("x-goog-api-key", apiKey)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(GeminiFunctionCallResponse.class)
                .block(Duration.ofSeconds(30));
    }

    private NlParseResult toParseResult(GeminiFunctionCallResponse response) {
        if (!response.hasFunctionCall()) {
            String text = response.getText();
            return NlParseResult.ofText(text.isEmpty() ? null : text);
        }

        Map<String, Object> args = response.getFunctionCall().args();
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> rawPlayers = (List<Map<String, Object>>) args.get("players");

        if (rawPlayers == null || rawPlayers.isEmpty()) {
            return NlParseResult.ofPlayers(List.of());
        }

        List<ParsedPlayer> players = rawPlayers.stream()
                .map(this::toPlayer)
                .toList();

        return NlParseResult.ofPlayers(players);
    }

    private ParsedPlayer toPlayer(Map<String, Object> raw) {
        String name = (String) raw.get("name");
        String studentNumber = (String) raw.get("studentNumber");
        Integer jerseyNumber = raw.get("jerseyNumber") instanceof Number n ? n.intValue() : null;
        return new ParsedPlayer(name, studentNumber, jerseyNumber);
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
}
