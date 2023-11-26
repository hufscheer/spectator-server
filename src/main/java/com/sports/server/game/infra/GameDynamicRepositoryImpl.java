package com.sports.server.game.infra;

import static com.sports.server.game.domain.QGame.game;
import static com.sports.server.sport.domain.QSport.sport;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sports.server.game.domain.Game;
import com.sports.server.game.domain.GameDynamicRepository;
import com.sports.server.game.domain.GameState;
import com.sports.server.game.dto.request.PageRequestDto;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class GameDynamicRepositoryImpl implements GameDynamicRepository {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<Game> findAllByLeagueAndStateAndSports(final Long leagueId, final GameState state,
                                                       final List<Long> sportIds, final PageRequestDto pageRequestDto) {
        DynamicBooleanBuilder booleanBuilder = DynamicBooleanBuilder.builder();
        Game lastGame = findLastGame(pageRequestDto.cursor());

        return jpaQueryFactory
                .selectFrom(game)
                .join(game.sport, sport).fetchJoin()
                .where(booleanBuilder
                        .and(
                                () -> game.startTime.eq(lastGame.getStartTime())
                                        .and(game.id.gt(pageRequestDto.cursor()))
                        )
                        .or(() -> game.startTime.after(lastGame.getStartTime()))
                        .and(() -> game.league.id.eq(leagueId))
                        .and(() -> game.state.eq(state))
                        .and(() -> game.sport.id.in(sportIds))
                        .build()
                )
                .orderBy(game.startTime.asc())
                .orderBy(game.id.asc())
                .limit(pageRequestDto.size())
                .fetch();
    }

    private Game findLastGame(final Long cursor) {
        return jpaQueryFactory
                .selectFrom(game)
                .where(game.id.eq(cursor))
                .fetchFirst();
    }

}
