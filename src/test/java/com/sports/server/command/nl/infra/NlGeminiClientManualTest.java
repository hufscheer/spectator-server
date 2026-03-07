package com.sports.server.command.nl.infra;

import com.sports.server.command.nl.dto.NlParseResult;
import com.sports.server.command.nl.dto.NlParseResult.ParsedPlayer;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

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
        NlParseResult result = nlGeminiClient.parsePlayers(
                "홍길동 202600001 10\n김철수 202600002 7\n이영희 202600003 5", List.of());

        assertThat(result.parsed()).isTrue();
        assertThat(result.players()).hasSizeGreaterThanOrEqualTo(3);
    }

    @Test
    @DisplayName("비정형 텍스트 파싱 - 괄호/쉼표 혼용")
    void 비정형_텍스트_파싱() {
        NlParseResult result = nlGeminiClient.parsePlayers(
                "홍길동(202600001) 10번, 김철수 202600002번 7, 이영희 / 202600003 / 5번", List.of());

        assertThat(result.parsed()).isTrue();
        assertThat(result.players()).isNotEmpty();
    }

    @Test
    @DisplayName("탭 구분 텍스트 파싱 - 엑셀 복붙")
    void 탭_구분_텍스트_파싱() {
        NlParseResult result = nlGeminiClient.parsePlayers(
                "홍길동\t202600001\t10\n김철수\t202600002\t7\n이영희\t202600003\t5", List.of());

        assertThat(result.parsed()).isTrue();
        assertThat(result.players()).hasSizeGreaterThanOrEqualTo(3);
    }

    @Test
    @DisplayName("등번호 없는 텍스트 파싱")
    void 등번호_없는_텍스트() {
        NlParseResult result = nlGeminiClient.parsePlayers(
                "홍길동 202600001\n김철수 202600002", List.of());

        assertThat(result.parsed()).isTrue();
        assertThat(result.players()).hasSize(2);
        assertThat(result.players().get(0).jerseyNumber()).isNull();
    }

    @Test
    @DisplayName("순서 뒤바뀐 텍스트 파싱")
    void 순서_뒤바뀐_텍스트() {
        NlParseResult result = nlGeminiClient.parsePlayers(
                "202600001 홍길동 10\n202600002 김철수 7", List.of());

        assertThat(result.parsed()).isTrue();
        assertThat(result.players()).hasSize(2);
    }
}
