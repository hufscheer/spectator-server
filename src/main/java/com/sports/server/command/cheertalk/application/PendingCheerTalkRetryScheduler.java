package com.sports.server.command.cheertalk.application;

import com.sports.server.command.cheertalk.domain.PendingCheerTalk;
import com.sports.server.command.cheertalk.domain.PendingCheerTalkRepository;
import com.sports.server.query.dto.response.CheerTalkResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PendingCheerTalkRetryScheduler {

    private final PendingCheerTalkRepository pendingCheerTalkRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Scheduled(fixedDelay = 2000)  // 2초 간격
    public void retryPendingCheerTalks() {

        List<PendingCheerTalk> pendingList =
                pendingCheerTalkRepository.findTop20Oldest(PageRequest.of(0, 20));

        if (pendingList.isEmpty()) {
            return;
        }

        for (PendingCheerTalk pending : pendingList) {
            try {
                messagingTemplate.convertAndSend(
                        pending.getDestination(),
                        new CheerTalkResponse.ForSpectator(
                                pending.getCheerTalk()
                        )
                );

                pendingCheerTalkRepository.delete(pending);

            } catch (Exception e) {
                log.warn(
                        "[Outbox 재전송 실패] id={}, destination={}, error={}",
                        pending.getId(), pending.getDestination(),
                        e.getMessage()
                );
            }
        }
    }
}
