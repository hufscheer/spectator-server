package com.sports.server.query.repository;

import static com.sports.server.command.comment.domain.QCheerTalk.cheerTalk;
import static com.sports.server.command.game.domain.QGameTeam.gameTeam;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sports.server.command.comment.domain.CheerTalk;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class CommentDynamicRepositoryImpl implements CommentDynamicRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<CheerTalk> findByGameIdOrderByStartTime(Long gameId, Long cursor, Integer size) {
        DynamicBooleanBuilder booleanBuilder = DynamicBooleanBuilder.builder();
        return queryFactory.selectFrom(cheerTalk)
                .join(gameTeam).on(cheerTalk.gameTeamId.eq(gameTeam.id))
                .where(gameTeam.game.id.eq(gameId))
                .where(booleanBuilder
                        .and(() -> cheerTalk.createdAt.loe(getCursorCreatedAt(cursor)))
                        .and(() -> cheerTalk.id.lt(cursor))
                        .build()
                )
                .orderBy(cheerTalk.createdAt.desc(), cheerTalk.id.desc())
                .limit(size)
                .fetch();
    }

    private LocalDateTime getCursorCreatedAt(final Long cursor) {
        if (cursor == null) {
            return null;
        }
        return queryFactory
                .select(cheerTalk.createdAt)
                .from(cheerTalk)
                .where(cheerTalk.id.eq(cursor))
                .fetchFirst();
    }
}
