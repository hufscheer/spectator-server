package com.sports.server.command.nl.dto;

public record NlRegisterTeamResponse(
        String displayMessage,
        Long teamId,
        Result result
) {
    public record Result(
            int created,
            int assigned,
            int skipped
    ) {
    }
}
