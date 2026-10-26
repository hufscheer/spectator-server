package com.sports.server.command.cheertalk.application;

import com.sports.server.command.cheertalk.domain.BotType;

public interface MaskingClient {
    String mask(String content);

    BotType botType();
}
