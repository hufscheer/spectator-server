package com.sports.server.command.timeline.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum GameProgressType {
    QUARTER_START("시작"),
    QUARTER_END("종료"),
    GAME_END("경기 종료");

    private final String displayName;
}
