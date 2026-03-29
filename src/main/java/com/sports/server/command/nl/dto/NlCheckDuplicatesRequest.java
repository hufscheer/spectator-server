package com.sports.server.command.nl.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record NlCheckDuplicatesRequest(
        @NotEmpty List<PlayerData> players
) {
    public record PlayerData(
            @NotNull String name,
            @NotNull String studentNumber,
            Integer jerseyNumber
    ) {
    }
}
