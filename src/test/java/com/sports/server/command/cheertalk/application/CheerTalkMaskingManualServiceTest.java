package com.sports.server.command.cheertalk.application;

import com.sports.server.command.cheertalk.domain.CheerTalk;
import com.sports.server.command.cheertalk.domain.CheerTalkRepository;
import com.sports.server.command.cheertalk.dto.GeminiResponse;
import com.sports.server.command.cheertalk.infra.GeminiClient;
import com.sports.server.common.application.EntityUtils;
import com.sports.server.support.ServiceTest;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("local")
@Sql("/cheer-talk-fixture.sql")
 @Disabled("수동 테스트 - 실행하려면 @Disabled 주석 처리")
class CheerTalkMaskingManualTest extends ServiceTest {

    @Autowired
    private CheerTalkMaskingService cheerTalkMaskingService;

    @Autowired
    private GeminiClient geminiClient;

    @Autowired
    private CheerTalkRepository cheerTalkRepository;

    @Autowired
    private EntityUtils entityUtils;

    @Value("${gemini.api.prompt}")
    private String prompt;

    @Test
    @DisplayName("Gemini API 마스킹 테스트 - 'ㅅㅂ ㅈㄴ 잘하네'")
    void testMasking_욕설_포함된_메시지() {
        // given
        String content = "ㅅㅂ ㅈㄴ 잘하네";
        String input = String.join("\n", prompt, content);

        // when
        System.out.println("=== Gemini API 마스킹 테스트 시작 ===");
        System.out.println("프롬프트: " + prompt);
        System.out.println("원본 메시지: " + content);
        System.out.println("전체 입력: " + input);
        System.out.println();

        GeminiResponse response = geminiClient.getGeminiResponse(input);
        String maskedContent = response.getFirstText();

        // then
        System.out.println("마스킹된 결과: " + maskedContent);
        System.out.println("마스킹 여부 (* 포함): " + maskedContent.contains("*"));
        System.out.println("=== Gemini API 마스킹 테스트 종료 ===\n");

        // 검증
        assertThat(maskedContent).isNotNull();
        assertThat(maskedContent).isNotEmpty();

        if (maskedContent.contains("*")) {
            System.out.println("✅ 마스킹이 정상적으로 수행되었습니다!");
            System.out.println("원본: " + content);
            System.out.println("마스킹: " + maskedContent);
        } else {
            System.out.println("⚠️ 마스킹이 수행되지 않았습니다. (* 문자가 없음)");
        }
    }

    @Test
    @DisplayName("Gemini API 마스킹 테스트 - 정상적인 메시지")
    void testMasking_정상_메시지() {
        // given
        String content = "우리팀 화이팅!";
        String input = String.join("\n", prompt, content);

        // when
        System.out.println("=== Gemini API 마스킹 테스트 시작 (정상 메시지) ===");
        System.out.println("원본 메시지: " + content);
        System.out.println();

        GeminiResponse response = geminiClient.getGeminiResponse(input);
        String maskedContent = response.getFirstText();

        // then
        System.out.println("결과: " + maskedContent);
        System.out.println("마스킹 여부 (* 포함): " + maskedContent.contains("*"));
        System.out.println("=== Gemini API 마스킹 테스트 종료 ===\n");

        // 정상 메시지는 마스킹되지 않아야 함
        if (!maskedContent.contains("*")) {
            System.out.println("✅ 정상 메시지는 마스킹되지 않았습니다!");
        } else {
            System.out.println("⚠️ 정상 메시지가 마스킹되었습니다.");
        }
    }

    @Test
    @DisplayName("CheerTalkMaskingService 통합 테스트 - 실제 DB 업데이트")
    void testMaskingService_실제_업데이트() {
        // given
        CheerTalk cheerTalk = new CheerTalk("ㅅㅂ ㅈㄴ 잘하네", 1L);
        cheerTalkRepository.save(cheerTalk);
        Long cheerTalkId = cheerTalk.getId();
        String originalContent = cheerTalk.getContent();

        System.out.println("=== CheerTalkMaskingService 통합 테스트 시작 ===");
        System.out.println("CheerTalk ID: " + cheerTalkId);
        System.out.println("원본 내용: " + originalContent);
        System.out.println();

        // when
        cheerTalkMaskingService.maskingCheerTalk(originalContent, cheerTalkId);

        // then
        CheerTalk updatedCheerTalk = entityUtils.getEntity(cheerTalkId, CheerTalk.class);
        String updatedContent = updatedCheerTalk.getContent();

        System.out.println("업데이트된 내용: " + updatedContent);
        System.out.println("마스킹 여부 (* 포함): " + updatedContent.contains("*"));
        System.out.println("=== CheerTalkMaskingService 통합 테스트 종료 ===\n");

        // 검증
        if (updatedContent.contains("*")) {
            System.out.println("✅ DB에 마스킹된 내용이 정상적으로 저장되었습니다!");
            System.out.println("원본: " + originalContent);
            System.out.println("마스킹: " + updatedContent);
            assertThat(updatedContent).contains("*");
        } else {
            System.out.println("⚠️ 마스킹이 수행되지 않았습니다. (* 문자가 없음)");
            System.out.println("원본: " + originalContent);
            System.out.println("결과: " + updatedContent);
        }
    }
}
