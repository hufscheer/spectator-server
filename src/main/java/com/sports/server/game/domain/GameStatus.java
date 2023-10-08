package com.sports.server.game.domain;

import lombok.Getter;

@Getter
public enum GameStatus {
    BEFORE(),
    FIRST_HALF(),
    BREAK_TIME(),
    SECOND_HALF(),
    END();
}
