package com.sports.server.command.nl.dto;

public record NlExecuteResponse(
        String displayMessage,
        Result result
) {
    public record Result(
            int created,
            int assigned,
            int skipped
    ) {
    }
}
