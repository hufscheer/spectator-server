package com.sports.server.command.game.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CheerCountUpdateRequest(
        @NotNull Long gameTeamId,
        @Min(1) @Max(499) int cheerCount
) {
}
