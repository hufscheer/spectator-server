package com.sports.server.command.nl.dto;

import java.util.List;
import java.util.Map;

public record NlProcessRequest(
        Long leagueId,
        Long teamId,
        List<Map<String, String>> history,
        String message
) {
}
