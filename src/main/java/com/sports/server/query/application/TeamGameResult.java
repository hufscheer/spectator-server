package com.sports.server.query.application;

import com.sports.server.command.game.domain.GameResult;

public record TeamGameResult(
        Long teamId,
        GameResult result,
        Long count
) {
}
