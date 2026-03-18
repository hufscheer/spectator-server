package com.sports.server.command.nl.dto;

import com.sports.server.command.nl.domain.PlayerStatus;

import java.util.List;

public record NlProcessResponse(
        String displayMessage,
        Preview preview
) {
    public record Preview(
            String type,
            Long teamId,
            String teamName,
            List<PlayerPreview> players,
            Summary summary,
            List<NlFailedLine> parseFailedLines
    ) {
    }

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
            int existingPlayers,
            int alreadyInTeam
    ) {
    }
}
