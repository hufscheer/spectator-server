package com.sports.server.command.cheertalk.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sports.server.command.cheertalk.domain.BotType;
import com.sports.server.command.cheertalk.domain.CheerTalk;
import com.sports.server.command.cheertalk.domain.CheerTalkBlockedByBotEvent;
import com.sports.server.command.cheertalk.domain.CheerTalkBotFilterHistory;
import com.sports.server.command.cheertalk.domain.CheerTalkBotFilterHistoryRepository;
import com.sports.server.command.cheertalk.domain.CheerTalkBotFilterResult;
import com.sports.server.common.application.EntityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CheerTalkBotFilterService {

    private final CheerTalkBotFilterHistoryRepository historyRepository;
    private final CheerTalkService cheerTalkService;
    private final EntityUtils entityUtils;
    private final ObjectMapper objectMapper;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 마스킹 모델이 내용을 바꾼 응원톡을 한 트랜잭션에서 마스킹 내용으로 바꾸고, 이력을 남기고, 차단한다.
     */
    @Transactional
    public void blockMasked(Long cheerTalkId, Long gameId, String original, String masked,
                            BotType botType, int latencyMs) {
        CheerTalk cheerTalk = entityUtils.getEntity(cheerTalkId, CheerTalk.class);
        cheerTalk.updateContent(masked);

        ObjectNode rawJson = objectMapper.createObjectNode();
        rawJson.put("original", original);
        rawJson.put("masked", masked);
        historyRepository.save(new CheerTalkBotFilterHistory(
                cheerTalk, CheerTalkBotFilterResult.ABUSIVE, botType, rawJson, latencyMs));

        cheerTalkService.blockByBot(cheerTalkId);
        eventPublisher.publishEvent(new CheerTalkBlockedByBotEvent(cheerTalkId, gameId));

        log.info("AI 모델 필터링으로 응원톡 차단: cheerTalkId={}", cheerTalkId);
    }
}
