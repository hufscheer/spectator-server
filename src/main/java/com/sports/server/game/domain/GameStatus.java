package com.sports.server.game.domain;

import lombok.Getter;

@Getter
public enum GameStatus {
    BEFORE(),
    FIRST_HALF(),
    SECOND_HALF(),
    OVER();
}
