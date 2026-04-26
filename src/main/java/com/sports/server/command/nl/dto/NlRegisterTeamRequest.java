package com.sports.server.command.nl.dto;

import com.sports.server.command.league.domain.SportType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record NlRegisterTeamRequest(
        @Valid @NotNull TeamInfo team,
        @NotEmpty @Valid List<PlayerData> players
) {
    public record TeamInfo(
            @NotBlank String name,
            @NotBlank String logoImageUrl,
            @NotBlank String unit,
            @NotBlank String teamColor,
            SportType sportType
    ) {
    }

    public record PlayerData(
            @NotNull String name,
            @NotNull String studentNumber,
            Integer jerseyNumber
    ) {
    }
}
