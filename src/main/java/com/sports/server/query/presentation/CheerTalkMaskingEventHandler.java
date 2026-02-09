package com.sports.server.query.presentation;

import com.sports.server.command.cheertalk.application.CheerTalkMaskingService;
import com.sports.server.command.cheertalk.domain.CheerTalk;
import com.sports.server.command.cheertalk.domain.CheerTalkMaskingEvent;
import com.sports.server.command.cheertalk.domain.PendingCheerTalk;
import com.sports.server.command.cheertalk.domain.PendingCheerTalkRepository;
import com.sports.server.command.cheertalk.dto.CheerTalkMaskedResponse;
import com.sports.server.query.dto.response.CheerTalkResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CheerTalkMaskingEventHandler {
    private static final String DESTINATION = "/topic/games/";
    private final SimpMessagingTemplate messagingTemplate;
    private final PendingCheerTalkRepository pendingCheerTalkRepository;
    private final CheerTalkMaskingService cheerTalkMaskingService;

    @EventListener
    @Async("asyncThreadPool")
    public void handle(CheerTalkMaskingEvent event) {
        CheerTalk cheerTalk = event.cheerTalk();
        String destination = DESTINATION + event.gameId();

        CheerTalkMaskedResponse cheerTalkMaskedResponse = cheerTalkMaskingService.maskingCheerTalk(
                cheerTalk.getContent(), cheerTalk.getId());

        if (!cheerTalkMaskedResponse.containsBadWord()) {
            return;
        }

        try {
            messagingTemplate.convertAndSend(
                    destination,
                    new CheerTalkResponse.ForSpectator(cheerTalk, cheerTalkMaskedResponse.maskedContent())
            );

        } catch (Exception e) {
            pendingCheerTalkRepository.save(
                    new PendingCheerTalk(
                            destination,
                            cheerTalk
                    )
            );

            log.error(
                    "CheerTalk WebSocket 전송 실패: cheerTalkId={}, gameTeamId={}, error={}",
                    cheerTalk.getId(), cheerTalk.getGameTeamId(), e.getMessage(), e
            );
        }
    }
}
