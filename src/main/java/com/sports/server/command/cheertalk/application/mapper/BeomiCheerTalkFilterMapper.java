package com.sports.server.command.cheertalk.application.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.sports.server.command.cheertalk.domain.BotType;
import com.sports.server.command.cheertalk.domain.CheerTalkBotFilterResult;
import com.sports.server.command.cheertalk.dto.CheerTalkFilterResponse;
import java.util.Comparator;
import java.util.stream.StreamSupport;
import org.springframework.stereotype.Component;

@Component
public class BeomiCheerTalkFilterMapper implements CheerTalkFilterResponseMapper {

    @Override
    public BotType supports() {
        return BotType.BEOMI;
    }

    @Override
    public CheerTalkFilterResponse map(JsonNode response, int latencyMs) {
        JsonNode concepts = response
                .get("outputs")
                .get(0)
                .get("data")
                .get("concepts");

        JsonNode top = StreamSupport.stream(concepts.spliterator(), false)
                .max(Comparator.comparingDouble(c -> c.get("value").asDouble()))
                .orElseThrow();

        String name = top.get("name").asText();
        double value = top.get("value").asDouble();

        CheerTalkBotFilterResult result =
                name.equals("offensive") || name.equals("hate")
                        ? CheerTalkBotFilterResult.ABUSIVE
                        : CheerTalkBotFilterResult.CLEAN;

        return new CheerTalkFilterResponse(
                result,
                value,
                BotType.BEOMI,
                response,
                latencyMs
        );
    }
}