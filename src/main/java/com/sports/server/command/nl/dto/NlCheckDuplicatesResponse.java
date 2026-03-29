package com.sports.server.command.nl.dto;

import java.util.List;

public record NlCheckDuplicatesResponse(
        List<DuplicatePlayer> duplicates
) {
    public record DuplicatePlayer(
            String studentNumber,
            String name
    ) {
    }
}
