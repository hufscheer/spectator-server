package com.sports.server.command.cheertalk.application;

import com.fasterxml.jackson.databind.JsonNode;
import com.sports.server.command.cheertalk.domain.BotType;
import com.sports.server.command.cheertalk.infra.BeomiClient;
import com.sports.server.command.cheertalk.infra.KorUnsmileClient;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
//@Disabled("수동 테스트 - 실행하려면 @Disabled 주석 처리")
class CheerTalkBotFilterManualTest {

    @Autowired
    private KorUnsmileClient korUnsmileClient;

    @Autowired
    private BeomiClient beomiClient;

    @Test
    @DisplayName("KorUnsmile API 호출 테스트 - 정상적인 응원톡")
    void testKorUnsmileClient_normalMessage() {
        // given
        String content = "우리팀 화이팅!";

        // when
        System.out.println("=== KorUnsmile API 호출 시작 ===");
        System.out.println("테스트 메시지: " + content);

        JsonNode response = korUnsmileClient.detectAbusiveContent(content);
        BotType botType = korUnsmileClient.supports();

        // then
        System.out.println("BotType: " + botType);
        System.out.println("응답: " + response.toPrettyString());
        System.out.println("=== KorUnsmile API 호출 종료 ===\n");
    }

    @Test
    @DisplayName("KorUnsmile API 호출 테스트 - 부적절한 내용")
    void testKorUnsmileClient_abusiveMessage() {
        // given
        String content = "ㅅㅂ 시발";

        // when
        System.out.println("=== KorUnsmile API 호출 시작 ===");
        System.out.println("테스트 메시지: " + content);

        JsonNode response = korUnsmileClient.detectAbusiveContent(content);
        BotType botType = korUnsmileClient.supports();

        // then
        System.out.println("BotType: " + botType);
        System.out.println("응답: " + response.toPrettyString());
        System.out.println("=== KorUnsmile API 호출 종료 ===\n");
    }

    @Test
    @DisplayName("Beomi API 호출 테스트 - 정상적인 응원톡")
    void testBeomiClient_normalMessage() {
        // given
        String content = "우리팀 화이팅!";

        // when
        System.out.println("=== Beomi API 호출 시작 ===");
        System.out.println("테스트 메시지: " + content);

        JsonNode response = beomiClient.detectAbusiveContent(content);
        BotType botType = beomiClient.supports();

        // then
        System.out.println("BotType: " + botType);
        System.out.println("응답: " + response.toPrettyString());
        System.out.println("=== Beomi API 호출 종료 ===\n");
    }

    @Test
    @DisplayName("Beomi API 호출 테스트 - 부적절한 내용")
    void testBeomiClient_abusiveMessage() {
        // given
        String content = "ㅅㅂ 시발";

        // when
        System.out.println("=== Beomi API 호출 시작 ===");
        System.out.println("테스트 메시지: " + content);

        JsonNode response = beomiClient.detectAbusiveContent(content);
        BotType botType = beomiClient.supports();

        // then
        System.out.println("BotType: " + botType);
        System.out.println("응답: " + response.toPrettyString());
        System.out.println("=== Beomi API 호출 종료 ===\n");
    }

    @Test
    @DisplayName("여러 메시지 연속 테스트")
    void testMultipleMessages() {
        String[] testMessages = {
                "화이팅!",
                "우리팀 최고!",
                "좋은 경기 감사합니다",
                "다음에도 응원할게요"
        };

        System.out.println("=== 여러 메시지 연속 테스트 시작 ===\n");

        for (String message : testMessages) {
            System.out.println("테스트 메시지: " + message);

            // KorUnsmile 테스트
            JsonNode korUnsmileResponse = korUnsmileClient.detectAbusiveContent(message);
            System.out.println("[KorUnsmile] 응답: " + korUnsmileResponse.toPrettyString());

            System.out.println("---\n");
        }

        System.out.println("=== 여러 메시지 연속 테스트 종료 ===");
    }
}
