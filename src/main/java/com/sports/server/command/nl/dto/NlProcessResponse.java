package com.sports.server.command.nl.dto;

import java.util.List;

public record NlProcessResponse(
        String displayMessage,
        Preview preview
) {
    public record Preview(
            List<ParsedPlayerPreview> players,
            int total,
            List<FailedLine> parseFailedLines
    ) {
    }

    public record ParsedPlayerPreview(
            String name,
            String studentNumber,
            Integer jerseyNumber
    ) {
    }

    public record FailedLine(
            int index,
            String studentNumber,
            String reason
    ) {
    }
}
