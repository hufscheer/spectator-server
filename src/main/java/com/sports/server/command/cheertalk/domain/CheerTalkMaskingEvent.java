package com.sports.server.command.cheertalk.domain;

public record CheerTalkMaskingEvent(
        CheerTalk cheerTalk,
        Long gameId
) {
}