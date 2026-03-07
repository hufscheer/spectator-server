package com.sports.server.command.nl.infra;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("local")
@Disabled("Gemini 외부 API 호출 테스트 - 수동 실행")
class NlGeminiClientManualTest {

    @Autowired
    private NlGeminiClient nlGeminiClient;

    @Test
    @DisplayName("정형 텍스트 파싱 - 공백 구분")
    void 정형_텍스트_공백_구분() {
        // given
        String message = "홍길동 202600001 10\n김철수 202600002 7\n이영희 202600003 5";

        // when
        GeminiFunctionCallResponse response = nlGeminiClient.parsePlayers(message, List.of());

        // then
        System.out.println("=== 정형 텍스트 파싱 테스트 ===");
        printResponse(response);

        assertThat(response.hasFunctionCall()).isTrue();
        assertThat(response.getFunctionCall().name()).isEqualTo("parse_players");
    }

    @Test
    @DisplayName("비정형 텍스트 파싱 - 괄호/쉼표 혼용")
    void 비정형_텍스트_파싱() {
        // given
        String message = "홍길동(202600001) 10번, 김철수 202600002번 7, 이영희 / 202600003 / 5번";

        // when
        GeminiFunctionCallResponse response = nlGeminiClient.parsePlayers(message, List.of());

        // then
        System.out.println("=== 비정형 텍스트 파싱 테스트 ===");
        printResponse(response);

        assertThat(response.hasFunctionCall()).isTrue();
    }

    @Test
    @DisplayName("탭 구분 텍스트 파싱 - 엑셀 복붙")
    void 탭_구분_텍스트_파싱() {
        // given
        String message = "홍길동\t202600001\t10\n김철수\t202600002\t7\n이영희\t202600003\t5";

        // when
        GeminiFunctionCallResponse response = nlGeminiClient.parsePlayers(message, List.of());

        // then
        System.out.println("=== 탭 구분 텍스트 파싱 테스트 ===");
        printResponse(response);

        assertThat(response.hasFunctionCall()).isTrue();
    }

    @Test
    @DisplayName("등번호 없는 텍스트 파싱")
    void 등번호_없는_텍스트() {
        // given
        String message = "홍길동 202600001\n김철수 202600002";

        // when
        GeminiFunctionCallResponse response = nlGeminiClient.parsePlayers(message, List.of());

        // then
        System.out.println("=== 등번호 없는 텍스트 파싱 테스트 ===");
        printResponse(response);

        assertThat(response.hasFunctionCall()).isTrue();
    }

    @Test
    @DisplayName("순서 뒤바뀐 텍스트 파싱")
    void 순서_뒤바뀐_텍스트() {
        // given
        String message = "202600001 홍길동 10\n202600002 김철수 7";

        // when
        GeminiFunctionCallResponse response = nlGeminiClient.parsePlayers(message, List.of());

        // then
        System.out.println("=== 순서 뒤바뀐 텍스트 파싱 테스트 ===");
        printResponse(response);

        assertThat(response.hasFunctionCall()).isTrue();
    }

    @SuppressWarnings("unchecked")
    private void printResponse(GeminiFunctionCallResponse response) {
        if (response.hasFunctionCall()) {
            GeminiFunctionCallResponse.FunctionCall fc = response.getFunctionCall();
            System.out.println("Function: " + fc.name());
            List<Map<String, Object>> players = (List<Map<String, Object>>) fc.args().get("players");
            if (players != null) {
                for (Map<String, Object> player : players) {
                    System.out.printf("  - %s / %s / %s%n",
                            player.get("name"), player.get("studentNumber"), player.get("jerseyNumber"));
                }
            }
        } else {
            System.out.println("Text response: " + response.getText());
        }
        System.out.println();
    }
}
