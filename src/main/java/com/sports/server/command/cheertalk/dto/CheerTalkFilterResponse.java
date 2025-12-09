package com.sports.server.command.cheertalk.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.sports.server.command.cheertalk.domain.BotType;
import com.sports.server.command.cheertalk.domain.CheerTalkFilterResult;
import java.util.Comparator;
import java.util.stream.StreamSupport;

public record CheerTalkFilterResponse(
        CheerTalkFilterResult result,
        double confidence,
        BotType botType,
        JsonNode rawResponse,
        int latencyMs
) {

    public CheerTalkFilterResponse fromKorUnsmile(JsonNode response, int latencyMs) {
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

    public CheerTalkFilterResponse fromBeomi(JsonNode response, int latencyMs) {

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

        CheerTalkFilterResult result =
                name.equals("offensive") || name.equals("hate")
                        ? CheerTalkFilterResult.ABUSIVE
                        : CheerTalkFilterResult.CLEAN;

        return new CheerTalkFilterResponse(
                result,
                value,
                BotType.BEOMI,
                response,
                latencyMs
        );
    }
}
