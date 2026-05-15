package com.sports.server.command.player.dto;

import java.util.List;

public record PlayerConflictResponse(
        String message,
        ConflictPlayer existingPlayer
) {
    public record ConflictPlayer(
            Long playerId,
            String name,
            String studentNumber,
            List<ConflictTeam> teams
    ) {}

    public record ConflictTeam(
            Long id,
            String name,
            String unit,
            String sportType
    ) {}
}
