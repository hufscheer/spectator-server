package com.sports.server.game.domain;

import lombok.Getter;

@Getter
public enum GameStatus {
    FIRST_HALF(),
    SECOND_HALF(),
    OVER();
}
