package com.sports.server.command.cheertalk.domain;

public record CheerTalkBlockedByBotEvent(
        Long cheerTalkId,
        Long gameId
) {
}
