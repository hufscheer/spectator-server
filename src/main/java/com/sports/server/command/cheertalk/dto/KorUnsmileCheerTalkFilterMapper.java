package com.sports.server.command.cheertalk.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.sports.server.command.cheertalk.domain.BotType;
import com.sports.server.command.cheertalk.domain.CheerTalkFilterResult;
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

        CheerTalkFilterResult result =
                label.equals("clean")
                        ? CheerTalkFilterResult.CLEAN
                        : CheerTalkFilterResult.ABUSIVE;

        return new CheerTalkFilterResponse(
                result,
                score,
                BotType.KOR_UNSMILE,
                response,
                latencyMs
        );
    }
}
