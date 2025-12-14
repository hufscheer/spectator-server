package com.sports.server.command.cheertalk.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.sports.server.command.cheertalk.domain.BotType;
import com.sports.server.command.cheertalk.domain.CheerTalkBotFilterResult;

public record CheerTalkFilterResponse(
        CheerTalkBotFilterResult result,
        double confidence,
        BotType botType,
        JsonNode rawResponse,
        int latencyMs
) {
}
