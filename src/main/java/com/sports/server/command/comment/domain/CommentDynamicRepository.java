package com.sports.server.command.comment.domain;

import java.util.List;

public interface CommentDynamicRepository {

    List<Comment> findByGameIdOrderByStartTime(Long gameId, Long cursor, Integer size);
}
