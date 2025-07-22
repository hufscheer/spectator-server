package com.sports.server.query.dto.response;

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
