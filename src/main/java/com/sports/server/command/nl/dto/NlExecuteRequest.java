package com.sports.server.command.nl.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record NlExecuteRequest(
        @NotNull Long leagueId,
        @NotNull Long teamId,
        @NotEmpty @Valid List<PlayerData> players
) {
    public record PlayerData(
            @NotNull String name,
            @NotNull String studentNumber,
            Integer jerseyNumber
    ) {
    }
}
