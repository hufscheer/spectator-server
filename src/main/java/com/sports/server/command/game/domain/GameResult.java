package com.sports.server.command.game.domain;

public enum GameResult {
    WIN("승리"),
    LOSE("패배"),
    DRAW("무승부"),
    NOT_DETERMINED("미정");

    private final String description;

    GameResult(String description) {
        this.description = description;
    }
}
