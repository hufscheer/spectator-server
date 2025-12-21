package com.sports.server.command.cheertalk.application;

import com.fasterxml.jackson.databind.JsonNode;
import com.sports.server.command.cheertalk.application.mapper.CheerTalkFilterResponseMapper;
import com.sports.server.command.cheertalk.application.mapper.CheerTalkFilterResponseMapperFinder;
import com.sports.server.command.cheertalk.domain.BotType;
import com.sports.server.command.cheertalk.domain.CheerTalk;
import com.sports.server.command.cheertalk.domain.CheerTalkBotFilterHistory;
import com.sports.server.command.cheertalk.domain.CheerTalkBotFilterHistoryRepository;
import com.sports.server.command.cheertalk.domain.CheerTalkBotFilterResult;
import com.sports.server.command.cheertalk.dto.CheerTalkFilterResponse;
import com.sports.server.command.cheertalk.infra.CheerTalkBotClient;
import com.sports.server.common.application.EntityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class CheerTalkBotFilterService {

    private final CheerTalkBotClient huggingfaceClient;
    private final CheerTalkFilterResponseMapperFinder mapperFinder;
    private final CheerTalkBotFilterHistoryRepository historyRepository;
    private final EntityUtils entityUtils;

    /**
     * ai 모델 이용한 응원톡 필터링 로직
     * @return CheerTalkBotFilterResult(CLEAN or ABUSIVE)
     */
    public CheerTalkBotFilterResult filterByBot(String content, Long cheerTalkId) {
        long startTime = System.currentTimeMillis();

        CheerTalk cheerTalk = entityUtils.getEntity(cheerTalkId, CheerTalk.class);

        JsonNode rawResponse = huggingfaceClient.detectAbusiveContent(content);
        int latencyMs = (int) (System.currentTimeMillis() - startTime);

        BotType botType = huggingfaceClient.supports();
        CheerTalkFilterResponseMapper mapper = mapperFinder.find(botType);

        CheerTalkFilterResponse response = mapper.map(rawResponse, latencyMs);

        CheerTalkBotFilterHistory history = new CheerTalkBotFilterHistory(
                cheerTalk,
                response.result(),
                response.botType(),
                response.rawResponse(),
                response.latencyMs()
        );
        historyRepository.save(history);

        return response.result();
    }
}