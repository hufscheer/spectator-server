package com.sports.server.query.dto.response;

import com.sports.server.command.league.domain.Quarter;

public record QuarterResponse(String key, String label) {
    public static QuarterResponse from(Quarter quarter) {
        return new QuarterResponse(quarter.name(), quarter.getDisplayName());
    }
}
