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
        return ofBulk(entities, size, sliced -> sliced.stream().map(mapper).toList(), cursorExtractor);
    }

    public static <E, T> CursorPageResponse<T> ofBulk(List<E> entities, int size,
                                                       Function<List<E>, List<T>> bulkMapper,
                                                       Function<E, Long> cursorExtractor) {
        boolean hasNext = entities.size() > size;
        List<E> sliced = hasNext ? entities.subList(0, size) : entities;
        List<T> content = bulkMapper.apply(sliced);
        Long nextCursor = (hasNext && !sliced.isEmpty()) ? cursorExtractor.apply(sliced.get(sliced.size() - 1)) : null;
        return new CursorPageResponse<>(content, nextCursor, hasNext);
    }
}
