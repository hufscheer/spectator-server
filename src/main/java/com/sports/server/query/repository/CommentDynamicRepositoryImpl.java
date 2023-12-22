package com.sports.server.query.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sports.server.command.comment.domain.Comment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

import static com.sports.server.command.comment.domain.QComment.comment;
import static com.sports.server.command.game.domain.QGameTeam.gameTeam;

@Repository
@RequiredArgsConstructor
public class CommentDynamicRepositoryImpl implements CommentDynamicRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Comment> findByGameIdOrderByStartTime(Long gameId, Long cursor, Integer size) {
        DynamicBooleanBuilder booleanBuilder = DynamicBooleanBuilder.builder();
        return queryFactory.selectFrom(comment)
                .join(gameTeam).on(comment.gameTeamId.eq(gameTeam.id))
                .where(gameTeam.game.id.eq(gameId))
                .where(booleanBuilder
                        .and(() -> comment.createdAt.loe(getCursorCreatedAt(cursor)))
                        .and(() -> comment.id.lt(cursor))
                        .build()
                )
                .orderBy(comment.createdAt.desc(), comment.id.desc())
                .limit(size)
                .fetch();
    }

    private LocalDateTime getCursorCreatedAt(final Long cursor) {
        if (cursor == null) {
            return null;
        }
        return queryFactory
                .select(comment.createdAt)
                .from(comment)
                .where(comment.id.eq(cursor))
                .fetchFirst();
    }
}
