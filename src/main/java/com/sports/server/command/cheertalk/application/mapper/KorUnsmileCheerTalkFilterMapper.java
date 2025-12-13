package com.sports.server.command.cheertalk.application.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.sports.server.command.cheertalk.domain.BotType;
import com.sports.server.command.cheertalk.domain.CheerTalkBotFilterResult;
import com.sports.server.command.cheertalk.dto.CheerTalkFilterResponse;
import org.springframework.stereotype.Component;

@Component
public class KorUnsmileCheerTalkFilterMapper implements CheerTalkFilterResponseMapper {

    @Override
    public BotType supports() {
        return BotType.KOR_UNSMILE;
    }

    @Override
    public CheerTalkFilterResponse map(JsonNode response, int latencyMs) {
        JsonNode top = response.get(0).get(0);

        String label = top.get("label").asText();
        double score = top.get("score").asDouble();

        CheerTalkBotFilterResult result =
                label.equals("clean")
                        ? CheerTalkBotFilterResult.CLEAN
                        : CheerTalkBotFilterResult.ABUSIVE;

        return new CheerTalkFilterResponse(
                result,
                score,
                BotType.KOR_UNSMILE,
                response,
                latencyMs
        );
    }
}
