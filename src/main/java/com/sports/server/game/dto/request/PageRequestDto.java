package com.sports.server.game.dto.request;

import lombok.NonNull;

public record PageRequestDto(

        @NonNull
        Long cursor,
        Integer size
) {
    private static final Integer DEFAULT_SIZE = 10;

    @Override
    public Integer size() {
        if (size == null) {
            return DEFAULT_SIZE;
        }

        return size;
    }
}
