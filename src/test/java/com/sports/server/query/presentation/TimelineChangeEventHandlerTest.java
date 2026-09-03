package com.sports.server.query.presentation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.sports.server.command.timeline.domain.TimelineChangedEvent;
import com.sports.server.query.dto.response.TimelineChangedResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.messaging.simp.SimpMessagingTemplate;

@DisplayName("타임라인 변경 알림은")
class TimelineChangeEventHandlerTest {

    private final SimpMessagingTemplate messagingTemplate = mock(SimpMessagingTemplate.class);
    private final TimelineChangeEventHandler handler = new TimelineChangeEventHandler(messagingTemplate);

    @Test
    void 응원톡과_다른_목적지로_보낸다() {
        // when
        handler.handle(TimelineChangedEvent.created(7L));

        // then
        ArgumentCaptor<String> destination = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Object> payload = ArgumentCaptor.forClass(Object.class);
        verify(messagingTemplate).convertAndSend(destination.capture(), payload.capture());

        assertThat(destination.getValue()).isEqualTo("/topic/games/7/timeline");
        assertThat(payload.getValue()).isEqualTo(new TimelineChangedResponse(7L, "CREATED"));
    }

    @Test
    void 삭제도_같은_목적지로_보낸다() {
        // when
        handler.handle(TimelineChangedEvent.deleted(7L));

        // then
        ArgumentCaptor<Object> payload = ArgumentCaptor.forClass(Object.class);
        verify(messagingTemplate).convertAndSend(anyString(), payload.capture());
        assertThat(payload.getValue()).isEqualTo(new TimelineChangedResponse(7L, "DELETED"));
    }

    @Test
    void 전송에_실패해도_예외를_밖으로_던지지_않는다() {
        // given: 알림 실패가 기록 등록·삭제를 되돌리면 안 된다
        willThrow(new RuntimeException("broker down"))
                .given(messagingTemplate).convertAndSend(anyString(), any(Object.class));

        // when & then
        handler.handle(TimelineChangedEvent.created(7L));
    }
}
