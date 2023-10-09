package com.sports.server.comment.domain;


import com.sports.server.game.domain.Game;
import org.springframework.data.repository.Repository;

import java.util.List;

public interface CommentRepository extends Repository<Comment, Long> {
    void save(Comment comment);

    List<Comment> getAllByGameOrderByCreatedAtDesc(final Game game);
}
