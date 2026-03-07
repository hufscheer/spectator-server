package com.sports.server.command.nl.infra;

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
public class NlGeminiClient {

    private final WebClient geminiWebClient;

    @Value("${gemini.api.key}")
    private String apiKey;

    private static final String SYSTEM_PROMPT = """
            너는 스포츠 리그 관리 시스템의 선수 등록 어시스턴트야.
            사용자가 입력한 텍스트에서 선수 정보를 추출하는 것이 너의 역할이야.

            각 선수에 대해 다음 정보를 추출해:
            - name: 선수 이름 (한글)
            - studentNumber: 학번 (정확히 9자리 숫자)
            - jerseyNumber: 등번호 (1~99 사이 숫자, 없으면 생략)

            입력 텍스트는 다양한 형태일 수 있어:
            - 공백/탭/쉼표로 구분된 형태
            - 괄호나 슬래시가 포함된 형태
            - 순서가 뒤바뀐 형태 (학번이 먼저 올 수도 있음)
            - 등번호가 없는 경우도 있음

            중요 규칙:
            - studentNumber는 반드시 원본 텍스트에 존재하는 9자리 연속 숫자여야 해
            - 9자리가 아닌 숫자를 학번으로 추측하거나 보정하지 마
            - 빈 줄이나 의미 없는 텍스트는 무시해
            - 파싱할 수 있는 선수 정보만 추출해

            반드시 parse_players 함수를 호출해서 응답해.
            """;

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

    public GeminiFunctionCallResponse parsePlayers(String message, List<Map<String, String>> history) {
        List<Map<String, Object>> contents = buildContents(message, history);

        Map<String, Object> body = Map.of(
                "systemInstruction", Map.of(
                        "parts", List.of(Map.of("text", SYSTEM_PROMPT))
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

    private List<Map<String, Object>> buildContents(String message, List<Map<String, String>> history) {
        List<Map<String, Object>> contents = new ArrayList<>();

        if (history != null) {
            for (Map<String, String> entry : history) {
                String role = entry.get("role");
                String content = entry.get("content");
                if (role != null && content != null) {
                    String geminiRole = "assistant".equals(role) ? "model" : "user";
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
