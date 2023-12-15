package com.sports.server.query.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sports.server.command.game.domain.GameState;
import com.sports.server.common.dto.PageRequestDto;
import com.sports.server.query.dto.request.GamesQueryRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;

import static com.sports.server.command.game.domain.QGame.game;

@Component
@RequiredArgsConstructor
public class GamesQueryConditionMapper {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final JPAQueryFactory jpaQueryFactory;

    public OrderSpecifier<?> mapOrderCondition(GamesQueryRequestDto request) {
        GameState state = GameState.from(request.getStateValue());
        if (state == GameState.FINISHED) {
            return game.startTime.stringValue().concat(game.id.stringValue()).desc();
        }
        return game.startTime.stringValue().concat(game.id.stringValue()).asc();
    }

    public BooleanBuilder mapBooleanCondition(GamesQueryRequestDto gamesQueryRequestDto,
                                              PageRequestDto pageRequestDto) {
        GameState state = GameState.from(gamesQueryRequestDto.getStateValue());
        String cursor = getCursorValue(pageRequestDto.cursor());
        DynamicBooleanBuilder booleanBuilder = DynamicBooleanBuilder.builder()
                .and(() -> game.league.id.eq(gamesQueryRequestDto.getLeagueId()))
                .and(() -> game.state.eq(state))
                .and(() -> game.sport.id.in(gamesQueryRequestDto.getSportIds()));
        if (state == GameState.FINISHED) {
            return booleanBuilder
                    .and(() -> game.startTime.stringValue().concat(game.id.stringValue()).lt(cursor))
                    .build();
        }
        return booleanBuilder
                .and(() -> game.startTime.stringValue().concat(game.id.stringValue()).gt(cursor))
                .build();
    }

    private String getCursorValue(Long cursor) {
        if (cursor == null) {
            return null;
        }
        return jpaQueryFactory
                .select(game.startTime)
                .from(game)
                .where(game.id.eq(cursor))
                .fetchFirst()
                .format(FORMATTER) + cursor;
    }
}
