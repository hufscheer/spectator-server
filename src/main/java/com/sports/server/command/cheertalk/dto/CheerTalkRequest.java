package com.sports.server.command.cheertalk.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.NonNull;

public record CheerTalkRequest(
        @NotBlank @Size(max = 255) String content,
        @NonNull Long gameTeamId
) {
}
