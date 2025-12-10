package com.sports.server.command.cheertalk.presentation;

import com.sports.server.command.cheertalk.application.CheerTalkService;
import com.sports.server.command.cheertalk.domain.CheerTalk;
import com.sports.server.command.cheertalk.domain.CheerTalkCreateEvent;
import com.sports.server.command.cheertalk.domain.CheerTalkFilterResult;
import com.sports.server.command.cheertalk.domain.CheerTalkRepository;
import com.sports.server.query.dto.response.CheerTalkResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class CheerTalkBotFilterEventHandler {

    private static final String DESTINATION = "/topic/games/";
    private final CheerTalkService cheerTalkService;
    private final CheerTalkRepository cheerTalkRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @TransactionalEventListener
    @Async("asyncThreadPool")
    public void handle(CheerTalkCreateEvent event) {
        CheerTalk cheerTalk = event.cheerTalk();

        try {
            CheerTalkFilterResult result = cheerTalkService.filter(
                    cheerTalk.getContent()
            );

            if (result == CheerTalkFilterResult.ABUSIVE) {
                blockCheerTalk(cheerTalk, event);
            } else {
                log.debug(
                        "외부 봇 필터링 완료: cheerTalkId={}, result={}",
                        cheerTalk.getId(),
                        result != null ? result : "CLEAN (fallback)"
                );
            }
        } catch (Exception e) {
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

    private void blockCheerTalk(CheerTalk cheerTalk, CheerTalkCreateEvent event) {
        cheerTalk.block();
        cheerTalkRepository.save(cheerTalk);

        String destination = DESTINATION + event.gameId();

        messagingTemplate.convertAndSend(
                destination,
                new CheerTalkResponse.ForSpectator(cheerTalk)
        );

        log.info(
                "AI 모델 필터링으로 응원톡 차단: cheerTalkId={}, content={}",
                cheerTalk.getId(),
                cheerTalk.getContent()
        );
    }
}
