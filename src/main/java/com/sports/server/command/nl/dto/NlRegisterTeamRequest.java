package com.sports.server.command.nl.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record NlRegisterTeamRequest(
        @NotNull Long leagueId,
        @Valid @NotNull TeamInfo team,
        @NotEmpty @Valid List<PlayerData> players
) {
    public record TeamInfo(
            @NotBlank String name,
            @NotBlank String logoImageUrl,
            @NotBlank String unit,
            @NotBlank String teamColor
    ) {
    }

    public record PlayerData(
            @NotNull String name,
            @NotNull String studentNumber,
            Integer jerseyNumber
    ) {
    }
}
