package com.sports.server.command.push.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PushTokenRefreshRequest(
        @NotBlank @Size(max = 512) String token
) {
}
