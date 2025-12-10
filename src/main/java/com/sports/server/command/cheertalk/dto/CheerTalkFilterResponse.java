package com.sports.server.command.cheertalk.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.sports.server.command.cheertalk.domain.BotType;
import com.sports.server.command.cheertalk.domain.CheerTalkFilterResult;

public record CheerTalkFilterResponse(
        CheerTalkFilterResult result,
        double confidence,
        BotType botType,
        JsonNode rawResponse,
        int latencyMs
) {
}
