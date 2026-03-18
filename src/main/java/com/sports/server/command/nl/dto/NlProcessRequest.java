package com.sports.server.command.nl.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.Map;

public record NlProcessRequest(
        List<Map<String, String>> history,
        @NotBlank @Size(max = 5000) String message
) {
}
