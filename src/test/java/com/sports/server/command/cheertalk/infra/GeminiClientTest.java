package com.sports.server.command.cheertalk.infra;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Disabled("Gemini 외부 API 호출 테스트 - 비용 및 네트워크 의존")
class GeminiClientTest {

    @Autowired
    private GeminiClient geminiClient;

    @Test
    void gemini_api_호출_성공() {
        // given
        String prompt = "욕설이 포함된 문장인지 판별해줘";

        // when
        String response = geminiClient.getGeminiResponse(prompt);

        // then
        assertThat(response).isNotNull();
        assertThat(response).isNotBlank();
    }
}
