package com.sports.server.command.nl.dto;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record NlCheckDuplicatesRequest(
        @NotEmpty List<String> studentNumbers
) {
}
