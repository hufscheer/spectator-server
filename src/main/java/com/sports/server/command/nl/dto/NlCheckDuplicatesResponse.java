package com.sports.server.command.nl.dto;

import com.sports.server.command.nl.domain.PlayerStatus;
import java.util.List;

public record NlCheckDuplicatesResponse(
        List<PlayerPreview> players,
        Summary summary
) {
    public record PlayerPreview(
            String name,
            String studentNumber,
            Integer jerseyNumber,
            PlayerStatus status,
            Long existingPlayerId
    ) {
    }

    public record Summary(
            int total,
            int newPlayers,
            int existingPlayers
    ) {
    }
}
