package com.sports.server.command.nl.dto;

public record NlFailedLine(
        int index,
        String studentNumber,
        String reason
) {
}
