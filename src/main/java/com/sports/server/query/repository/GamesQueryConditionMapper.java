package com.sports.server.query.repository;

import static com.sports.server.command.game.domain.QGame.game;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sports.server.command.game.domain.GameState;
import com.sports.server.common.dto.PageRequestDto;
import com.sports.server.query.dto.request.GamesQueryRequestDto;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GamesQueryConditionMapper {

    private static final OrderSpecifier<?>[] FINISHED_ORDER = {game.startTime.desc(), game.id.desc()};
    private static final OrderSpecifier<?>[] NOT_FINISHED_ORDER = {game.startTime.asc(), game.id.asc()};

    private final JPAQueryFactory jpaQueryFactory;
    private final GameTeamDynamicRepository gameTeamDynamicRepository;

    public OrderSpecifier<?>[] mapOrderCondition(GamesQueryRequestDto request) {
        GameState state = GameState.from(request.getStateValue());
        if (state == GameState.FINISHED) {
            return FINISHED_ORDER;
        }
        return NOT_FINISHED_ORDER;
    }

    public BooleanBuilder mapBooleanCondition(GamesQueryRequestDto gamesQueryRequestDto,
                                              PageRequestDto pageRequestDto) {
        GameState state = GameState.from(gamesQueryRequestDto.getStateValue());
        Long cursor = pageRequestDto.cursor();
        LocalDateTime cursorStartTime = getCursorStartTime(cursor);
        DynamicBooleanBuilder booleanBuilder = DynamicBooleanBuilder.builder()
                .and(() -> game.league.id.eq(gamesQueryRequestDto.getLeagueId()))
                .and(() -> game.state.eq(state))
                .and(() -> game.sport.id.in(gamesQueryRequestDto.getSportIds()))
                .and(() -> game.round.in(gamesQueryRequestDto.getRound()))
                .and(
                        () -> game.id.in(
                                gameTeamDynamicRepository.findAllByLeagueTeamIds(
                                        gamesQueryRequestDto.getLeagueTeamIds())));
        return booleanBuilder
                .and(() -> game.startTime.eq(cursorStartTime).and(game.id.gt(cursor))
                        .or(game.startTime.gt(cursorStartTime)))
                .build();
    }

    private LocalDateTime getCursorStartTime(Long cursor) {
        if (cursor == null) {
            return null;
        }
        return jpaQueryFactory
                .select(game.startTime)
                .from(game)
                .where(game.id.eq(cursor))
                .fetchOne();
    }
}
