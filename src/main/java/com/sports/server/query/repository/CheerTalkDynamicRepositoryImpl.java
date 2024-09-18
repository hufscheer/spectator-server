package com.sports.server.query.repository;

import static com.sports.server.command.cheertalk.domain.QCheerTalk.cheerTalk;
import static com.sports.server.command.game.domain.QGameTeam.gameTeam;
import static com.sports.server.command.report.domain.QReport.report;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sports.server.command.cheertalk.domain.CheerTalk;
import com.sports.server.command.report.domain.ReportState;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class CheerTalkDynamicRepositoryImpl implements CheerTalkDynamicRepository {

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

    @Override
    public List<CheerTalk> findReportedCheerTalksByLeagueId(Long leagueId, Long cursor, Integer size) {
        DynamicBooleanBuilder booleanBuilder = DynamicBooleanBuilder.builder();
        return queryFactory.selectFrom(cheerTalk)
                .join(gameTeam).on(cheerTalk.gameTeamId.eq(gameTeam.id))
                .join(report).on(report.cheerTalk.eq(cheerTalk))
                .where(report.state.eq(ReportState.PENDING))
                .where(gameTeam.game.league.id.eq(leagueId))
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
