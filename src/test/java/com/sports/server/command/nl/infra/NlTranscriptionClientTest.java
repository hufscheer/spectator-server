package com.sports.server.command.nl.infra;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sports.server.common.exception.CustomException;
import com.sports.server.common.infra.openrouter.OpenRouterChatCaller;
import com.sports.server.common.infra.openrouter.OpenRouterChatResponse;
import java.time.Duration;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpStatus;

class NlTranscriptionClientTest {

    private final OpenRouterChatCaller caller = mock(OpenRouterChatCaller.class);
    @SuppressWarnings("unchecked")
    private final ObjectProvider<OpenRouterChatCaller> provider = mock(ObjectProvider.class);
    private NlTranscriptionClient client;

    NlTranscriptionClientTest() {
        when(provider.getIfAvailable()).thenReturn(caller);
        client = new NlTranscriptionClient(provider, "test/model", "PROMPT", "minimal", 4000);
    }

    private static OpenRouterChatResponse reply(String content) {
        return new OpenRouterChatResponse(List.of(
                new OpenRouterChatResponse.Choice(new OpenRouterChatResponse.Message(content, null))));
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> capturedBody() {
        ArgumentCaptor<Map<String, Object>> captor = ArgumentCaptor.forClass(Map.class);
        verify(caller).call(captor.capture(), eq(Duration.ofSeconds(60)));
        return captor.getValue();
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> contentParts(Map<String, Object> body) {
        List<Map<String, Object>> messages = (List<Map<String, Object>>) body.get("messages");
        assertThat(messages).hasSize(1);
        assertThat(messages.get(0).get("role")).isEqualTo("user");
        return (List<Map<String, Object>>) messages.get(0).get("content");
    }

    @Test
    void 이미지는_image_url_파트로_보낸다() {
        byte[] png = {1, 2, 3};
        when(caller.call(any(), any())).thenReturn(reply("홍길동\t202312345"));

        String text = client.transcribe(png, "image/png");

        assertThat(text).isEqualTo("홍길동\t202312345");
        Map<String, Object> body = capturedBody();
        assertThat(body.get("model")).isEqualTo("test/model");
        assertThat(body.get("reasoning")).isEqualTo(Map.of("effort", "minimal"));
        assertThat(body.get("max_tokens")).isEqualTo(4000);
        List<Map<String, Object>> parts = contentParts(body);
        assertThat(parts.get(0)).isEqualTo(Map.of("type", "text", "text", "PROMPT"));
        assertThat(parts.get(1)).isEqualTo(Map.of(
                "type", "image_url",
                "image_url", Map.of("url", "data:image/png;base64," + Base64.getEncoder().encodeToString(png))));
    }

    @Test
    void PDF는_file_파트로_보낸다() {
        byte[] pdf = {'%', 'P', 'D', 'F'};
        when(caller.call(any(), any())).thenReturn(reply("x"));

        client.transcribe(pdf, "application/pdf");

        List<Map<String, Object>> parts = contentParts(capturedBody());
        assertThat(parts.get(1)).isEqualTo(Map.of(
                "type", "file",
                "file", Map.of("filename", "roster.pdf",
                        "file_data", "data:application/pdf;base64," + Base64.getEncoder().encodeToString(pdf))));
    }

    @Test
    void 코드_펜스를_벗기고_다듬는다() {
        assertThat(transcribeReply("  ```\n홍길동\t1\n```  ")).isEqualTo("홍길동\t1");
        assertThat(transcribeReply("```text\na\tb\n```")).isEqualTo("a\tb");
        assertThat(transcribeReply("  평문  ")).isEqualTo("평문");
        assertThat(transcribeReply(null)).isEmpty();
    }

    private String transcribeReply(String content) {
        when(caller.call(any(), any())).thenReturn(reply(content));
        return client.transcribe(new byte[]{1}, "image/png");
    }

    @Test
    void 호출이_실패하면_503을_낸다() {
        when(caller.call(any(), any())).thenThrow(new IllegalStateException("Timeout on blocking read"));

        assertThatThrownBy(() -> client.transcribe(new byte[]{1}, "image/png"))
                .isInstanceOfSatisfying(CustomException.class,
                        e -> assertThat(e.getStatus()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE));
    }

    @Test
    void 호출기가_없으면_503을_낸다() {
        when(provider.getIfAvailable()).thenReturn(null);

        assertThatThrownBy(() -> client.transcribe(new byte[]{1}, "image/png"))
                .isInstanceOfSatisfying(CustomException.class,
                        e -> assertThat(e.getStatus()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE));
    }
}
