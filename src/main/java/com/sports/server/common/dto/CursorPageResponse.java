package com.sports.server.common.dto;

import java.util.List;
import java.util.function.Function;

public record CursorPageResponse<T>(
        List<T> content,
        Long nextCursor,
        boolean hasNext
) {
    public static <E, T> CursorPageResponse<T> of(List<E> entities, int size,
                                                   Function<E, T> mapper,
                                                   Function<E, Long> cursorExtractor) {
        boolean hasNext = entities.size() > size;
        List<E> sliced = hasNext ? entities.subList(0, size) : entities;
        List<T> content = sliced.stream().map(mapper).toList();
        Long nextCursor = hasNext ? cursorExtractor.apply(sliced.get(sliced.size() - 1)) : null;
        return new CursorPageResponse<>(content, nextCursor, hasNext);
    }
}