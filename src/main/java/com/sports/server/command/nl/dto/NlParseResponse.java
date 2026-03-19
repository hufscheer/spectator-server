package com.sports.server.command.nl.dto;

import java.util.List;

public record NlParseResponse(
        String displayMessage,
        Preview preview
) {
    public record Preview(
            List<ParsedPlayerPreview> players,
            int total,
            List<NlFailedLine> parseFailedLines
    ) {
    }

    public record ParsedPlayerPreview(
            String name,
            String studentNumber,
            Integer jerseyNumber
    ) {
    }
}
