package com.sports.server.command.cheertalk.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sports.server.command.cheertalk.presentation.BotBlockedCheerTalkSpectatorPublishHandler;
import com.sports.server.common.application.EntityUtils;
import com.sports.server.query.dto.response.CheerTalkResponse;
import com.sports.server.query.presentation.CheerTalkEventHandler;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.messaging.simp.SimpMessagingTemplate;

@DisplayName("응원톡 작성자 키는")
class CheerTalkAuthorKeyTest {

    private static final String VISITOR_ID = "123e4567-e89b-42d3-a456-426614174000";

    private static String sha256Prefix16(String value) throws Exception {
        byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
        return HexFormat.of().formatHex(digest).substring(0, 16);
    }

    @Test
    void 방문자_ID의_SHA256_앞_16자리_소문자_hex이다() throws Exception {
        CheerTalk cheerTalk = new CheerTalk("파이팅", 1L, VISITOR_ID);

        assertThat(cheerTalk.getAuthorKey())
                .isEqualTo(sha256Prefix16(VISITOR_ID))
                .matches("[0-9a-f]{16}");
    }

    @Test
    void 방문자가_없으면_null이다() {
        assertThat(new CheerTalk("파이팅", 1L).getAuthorKey()).isNull();
        assertThat(new CheerTalk("파이팅", 1L, (String) null).getAuthorKey()).isNull();
    }

    @Test
    void AI_시드_응원톡은_null이다() {
        assertThat(CheerTalk.createAiSeed("파이팅", 1L).getAuthorKey()).isNull();
    }

    @Test
    void 관중용_응답에_담긴다() throws Exception {
        CheerTalk cheerTalk = new CheerTalk("파이팅", 1L, VISITOR_ID);

        assertThat(new CheerTalkResponse.ForSpectator(cheerTalk).authorKey()).isEqualTo(sha256Prefix16(VISITOR_ID));
        assertThat(new CheerTalkResponse.ForSpectator(cheerTalk, "마스킹").authorKey())
                .isEqualTo(sha256Prefix16(VISITOR_ID));
        assertThat(new CheerTalkResponse.ForSpectator(CheerTalk.createAiSeed("파이팅", 1L)).authorKey()).isNull();
    }

    @Test
    void 새_응원톡_실시간_메시지에_담긴다() throws Exception {
        SimpMessagingTemplate template = mock(SimpMessagingTemplate.class);
        CheerTalkEventHandler handler = new CheerTalkEventHandler(template, mock(ApplicationEventPublisher.class));

        handler.handle(new CheerTalkCreateEvent(new CheerTalk("파이팅", 1L, VISITOR_ID), 3L));

        ArgumentCaptor<Object> payload = ArgumentCaptor.forClass(Object.class);
        verify(template).convertAndSend(anyString(), payload.capture());
        assertThat(((CheerTalkResponse.ForSpectator) payload.getValue()).authorKey())
                .isEqualTo(sha256Prefix16(VISITOR_ID));
    }

    @Test
    void 봇_차단_실시간_메시지에_담긴다() throws Exception {
        SimpMessagingTemplate template = mock(SimpMessagingTemplate.class);
        EntityUtils entityUtils = mock(EntityUtils.class);
        CheerTalk cheerTalk = new CheerTalk("파이팅", 1L, VISITOR_ID);
        when(entityUtils.getEntity(any(Long.class), any(Class.class))).thenReturn(cheerTalk);
        BotBlockedCheerTalkSpectatorPublishHandler handler =
                new BotBlockedCheerTalkSpectatorPublishHandler(template, entityUtils);

        handler.handle(new CheerTalkBlockedByBotEvent(5L, 3L));

        ArgumentCaptor<Object> payload = ArgumentCaptor.forClass(Object.class);
        verify(template).convertAndSend(anyString(), payload.capture());
        assertThat(((CheerTalkResponse.ForSpectator) payload.getValue()).authorKey())
                .isEqualTo(sha256Prefix16(VISITOR_ID));
    }
}
