package com.sports.server.command.cheertalk.infra;

import com.fasterxml.jackson.databind.JsonNode;
import com.sports.server.command.cheertalk.domain.BotType;

public interface CheerTalkBotClient {

    BotType supports();

    JsonNode detectAbusiveContent(String content);
}