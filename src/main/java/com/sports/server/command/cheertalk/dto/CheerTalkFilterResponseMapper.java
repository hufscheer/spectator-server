package com.sports.server.command.cheertalk.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.sports.server.command.cheertalk.domain.BotType;

public interface CheerTalkFilterResponseMapper {

    BotType supports();

    CheerTalkFilterResponse map(JsonNode response, int latencyMs);
}