package com.sports.server.query.dto;

import com.sports.server.command.player.domain.Player;

public record PlayerGoalSummary(
        Player player,
        Long totalGoals
) {
}