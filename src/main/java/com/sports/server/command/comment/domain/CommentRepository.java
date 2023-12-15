package com.sports.server.command.comment.domain;


import org.springframework.data.repository.Repository;

import java.util.Optional;

public interface CommentRepository extends Repository<Comment, Long> {
    void save(Comment comment);

    Optional<Comment> findById(Long id);
}
