package com.sports.server.comment.domain;


import com.sports.server.game.domain.Game;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import java.util.List;

public interface CommentRepository extends Repository<Comment, Long> {
    void save(Comment comment);

    @Query("select c from Comment c " +
            "inner join c.gameTeam gt where gt.game = :game " +
            "order by c.createdAt desc")
    List<Comment> getAllByGameOrderByCreatedAtDesc(final Game game);
}
