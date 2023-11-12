package com.sports.server.comment.domain;


import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CommentRepository extends Repository<Comment, Long> {
    void save(Comment comment);

    @Query("select c from Comment c " +
            "inner join GameTeam gt on c.gameTeamId = gt.id " +
            "where gt.game.id = :gameId " +
            "order by c.createdAt desc")
    List<Comment> getAllByGameOrderByCreatedAtDesc(@Param("gameId") final Long gameId);

    Optional<Comment> findById(Long id);
}
