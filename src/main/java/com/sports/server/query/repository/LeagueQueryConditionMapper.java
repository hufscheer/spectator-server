package com.sports.server.query.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sports.server.command.league.domain.LeagueProgress;
import com.sports.server.common.dto.PageRequestDto;
import com.sports.server.query.dto.request.LeagueQueryRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

import static com.sports.server.command.league.domain.QLeague.league;

@Component
@RequiredArgsConstructor
public class LeagueQueryConditionMapper {

    private final JPAQueryFactory jpaQueryFactory;

    public BooleanBuilder mapBooleanCondition(LeagueQueryRequestDto requestDto, PageRequestDto pageRequest) {
        BooleanBuilder conditions = new BooleanBuilder();

        conditions.and(getYearCondition(requestDto.year()));
        conditions.and(getProgressCondition(requestDto.leagueProgress()));
        conditions.and(cursorPagination(pageRequest.cursor()));

        return conditions;
    }

    private BooleanExpression getYearCondition(Integer year) {
        if (year == null) {
            return null;
        }
        return league.startAt.year().eq(year);
    }

    private BooleanExpression getProgressCondition(LeagueProgress progress) {
        if (progress == null) {
            return null;
        }

        LocalDateTime now = LocalDateTime.now();
        return switch (progress) {
            case BEFORE_START -> league.startAt.gt(now);
            case IN_PROGRESS -> league.startAt.loe(now).and(league.endAt.goe(now));
            case FINISHED -> league.endAt.lt(now);
        };
    }

    private BooleanExpression cursorPagination(Long cursor) {
        if (cursor == null) {
            return null;
        }
        LocalDateTime cursorStartTime = getCursorStartTime(cursor);
        return league.startAt.lt(cursorStartTime)
                .or(league.startAt.eq(cursorStartTime).and(league.id.lt(cursor)));
    }

    private LocalDateTime getCursorStartTime(Long cursor) {
        return jpaQueryFactory
                .select(league.startAt)
                .from(league)
                .where(league.id.eq(cursor))
                .fetchOne();
    }
}
