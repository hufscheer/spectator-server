package com.sports.server.command.cheertalk.application.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.sports.server.command.cheertalk.domain.BotType;
import com.sports.server.command.cheertalk.dto.CheerTalkFilterResponse;

public interface CheerTalkFilterResponseMapper {

    BotType supports();

    CheerTalkFilterResponse map(JsonNode response, int latencyMs);
}