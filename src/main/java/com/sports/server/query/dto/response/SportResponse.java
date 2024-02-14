package com.sports.server.query.dto.response;

import com.sports.server.command.sport.domain.Sport;

public record SportResponse(
        Long id,
        String name

) {
    public SportResponse(Sport sport) {
        this(
                sport.getId(),
                sport.getName()
        );
    }
}
