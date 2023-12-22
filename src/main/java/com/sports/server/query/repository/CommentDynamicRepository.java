package com.sports.server.query.repository;

import com.sports.server.command.comment.domain.Comment;

import java.util.List;

public interface CommentDynamicRepository {

    List<Comment> findByGameIdOrderByStartTime(Long gameId, Long cursor, Integer size);
}
