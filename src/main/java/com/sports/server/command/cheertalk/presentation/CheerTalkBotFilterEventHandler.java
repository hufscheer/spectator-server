package com.sports.server.command.cheertalk.presentation;

import com.sports.server.command.cheertalk.application.CheerTalkService;
import com.sports.server.command.cheertalk.domain.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class CheerTalkBotFilterEventHandler {

    private final CheerTalkService cheerTalkService;
    private final ApplicationEventPublisher eventPublisher;

    @TransactionalEventListener
    @Async("asyncThreadPool")
    @Transactional
    public void handle(CheerTalkCreateEvent event) {
        CheerTalk cheerTalk = event.cheerTalk();

        try {
            CheerTalkBotFilterResult result = cheerTalkService.filterByBot(
                    cheerTalk.getContent()
            );

            if (result == CheerTalkBotFilterResult.ABUSIVE) {
                Long cheerTalkId = cheerTalk.getId();
                
                cheerTalkService.blockById(cheerTalkId);
                eventPublisher.publishEvent(
                        new CheerTalkBlockedByBotEvent(cheerTalkId, event.gameId())
                );

                log.info(
                        "AI 모델 필터링으로 응원톡 차단: cheerTalkId={}, content={}",
                        cheerTalkId,
                        cheerTalk.getContent()
                );
            }
        } catch (RuntimeException e) {
            log.error(
                    "외부 봇 필터링 처리 중 오류: cheerTalkId={}",
                    cheerTalk.getId(),
                    e
            );
            /**
             * 추후 재시도 로직 추가
             */
        }
    }
}
