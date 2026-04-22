package com.sports.server.command.nl.dto;

public record NlFailedLine(
        int index,
        String studentNumber,
        String name,
        Integer jerseyNumber,
        String reason
) {
}
