package com.sports.server.query.repository;

import static com.sports.server.command.cheertalk.domain.QCheerTalk.cheerTalk;
import static com.sports.server.command.game.domain.QGame.game;
import static com.sports.server.command.game.domain.QGameTeam.gameTeam;
import static com.sports.server.command.league.domain.QLeague.league;
import static com.sports.server.command.member.domain.QMember.member;
import static com.sports.server.command.report.domain.QReport.report;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
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
        return applyPagination(
                queryFactory.selectFrom(cheerTalk)
                        .join(gameTeam).on(cheerTalk.gameTeamId.eq(gameTeam.id))
                        .where(gameTeam.game.id.eq(gameId)),
                cursor,
                size
        );
    }

    @Override
    public List<CheerTalk> findReportedCheerTalksByAdminId(Long adminId, Long cursor, Integer size) {
        return applyPagination(
                queryFactory.selectFrom(cheerTalk)
                        .join(gameTeam).on(cheerTalk.gameTeamId.eq(gameTeam.id))
                        .join(game).on(gameTeam.game.id.eq(game.id))
                        .join(league).on(game.league.id.eq(league.id))
                        .join(member).on(league.administrator.id.eq(member.id))
                        .join(report).on(report.cheerTalk.eq(cheerTalk))
                        .where(report.state.eq(ReportState.PENDING))
                        .where(member.id.eq(adminId)),
                cursor,
                size
        );
    }

    @Override
    public List<CheerTalk> findUnblockedCheerTalksByAdminId(Long adminId, Long cursor, Integer size) {
        return applyPagination(
                queryFactory.selectFrom(cheerTalk)
                        .join(gameTeam).on(cheerTalk.gameTeamId.eq(gameTeam.id))
                        .join(game).on(gameTeam.game.id.eq(game.id))
                        .join(league).on(game.league.id.eq(league.id))
                        .join(member).on(league.administrator.id.eq(member.id))
                        .where(member.id.eq(adminId))
                        .where(cheerTalk.isBlocked.eq(false)),
                cursor,
                size
        );
    }

    @Override
    public List<CheerTalk> findBlockedCheerTalksByAdminId(Long adminId, Long cursor, Integer size) {
        return applyPagination(
                queryFactory.selectFrom(cheerTalk)
                        .join(gameTeam).on(cheerTalk.gameTeamId.eq(gameTeam.id))
                        .join(game).on(gameTeam.game.id.eq(game.id))
                        .join(league).on(game.league.id.eq(league.id))
                        .join(member).on(league.administrator.id.eq(member.id))
                        .where(member.id.eq(adminId))
                        .where(cheerTalk.isBlocked.eq(true)),
                cursor,
                size
        );
    }

    private List<CheerTalk> applyPagination(JPAQuery<CheerTalk> query, Long cursor, Integer size) {
        return query
                .where(getPaginationConditions(cursor))
                .orderBy(cheerTalk.createdAt.desc(), cheerTalk.id.desc())
                .limit(size)
                .fetch();
    }

    private BooleanExpression getPaginationConditions(Long cursor) {
        if (cursor == null) {
            return null;
        }
        return cheerTalk.createdAt.loe(getCursorCreatedAt(cursor))
                .and(cheerTalk.id.lt(cursor));
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