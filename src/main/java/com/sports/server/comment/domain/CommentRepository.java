package com.sports.server.comment.domain;


import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentRepository extends Repository<Comment, Long> {
    void save(Comment comment);

    @Query("select c from Comment c " +
            "inner join GameTeam gt on c.gameTeamId = gt.id " +
            "where gt.game.id = :gameId " +
            "order by c.createdAt desc")
    List<Comment> getAllByGameOrderByCreatedAtDesc(@Param("gameId") final Long gameId);
}
