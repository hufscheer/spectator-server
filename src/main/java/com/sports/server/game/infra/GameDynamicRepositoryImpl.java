package com.sports.server.game.infra;

import static com.sports.server.game.domain.QGame.game;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sports.server.game.domain.Game;
import com.sports.server.game.domain.GameDynamicRepository;
import com.sports.server.game.domain.GameState;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class GameDynamicRepositoryImpl implements GameDynamicRepository {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<Game> findAllByLeagueAndStateAndSports(final Long leagueId, final GameState state,
                                                       final List<Long> sportIds) {
        DynamicBooleanBuilder booleanBuilder = DynamicBooleanBuilder.builder();
        return jpaQueryFactory
                .selectFrom(game)
                .where(booleanBuilder
                        .and(() -> game.league.id.eq(leagueId))
                        .and(() -> game.state.eq(state))
                        .and(() -> game.sport.id.in(sportIds))
                        .build()
                ).orderBy(game.startTime.asc())
                .orderBy(game.id.asc())
                .fetch();
    }
}
