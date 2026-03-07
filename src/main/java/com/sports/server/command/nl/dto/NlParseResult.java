package com.sports.server.command.nl.dto;

import java.util.List;

public record NlParseResult(
        boolean parsed,
        String textMessage,
        List<ParsedPlayer> players
) {
    public record ParsedPlayer(
            String name,
            String studentNumber,
            Integer jerseyNumber
    ) {
    }

    public static NlParseResult ofText(String message) {
        return new NlParseResult(false, message, List.of());
    }

    public static NlParseResult ofPlayers(List<ParsedPlayer> players) {
        return new NlParseResult(true, null, players);
    }
}
