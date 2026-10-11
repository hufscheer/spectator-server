package com.sports.server.command.push.dto;

import com.sports.server.command.push.domain.PushPlatform;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PushDeviceRegisterRequest(
        @NotBlank @Size(max = 512) String token,
        @NotNull PushPlatform platform
) {
}
