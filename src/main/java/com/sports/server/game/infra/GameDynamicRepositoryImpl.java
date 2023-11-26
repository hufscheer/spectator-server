package com.sports.server.game.infra;

import static com.sports.server.game.domain.QGame.game;
import static com.sports.server.sport.domain.QSport.sport;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sports.server.game.domain.Game;
import com.sports.server.game.domain.GameDynamicRepository;
import com.sports.server.game.domain.GameState;
import com.sports.server.game.dto.request.PageRequestDto;
import java.time.LocalDateTime;
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
        LocalDateTime lastStartTime = findLastStartTime(pageRequestDto.cursor());

        return jpaQueryFactory
                .selectFrom(game)
                .join(game.sport, sport).fetchJoin()
                .where(booleanBuilder
                        .and(() -> game.startTime.goe(lastStartTime)
                                .and(game.id.gt(pageRequestDto.cursor())))
                        .and(() -> game.league.id.eq(leagueId))
                        .and(() -> game.state.eq(state))
                        .and(() -> game.sport.id.in(sportIds))
                        .build()
                )
                .orderBy(game.startTime.asc(), game.id.asc())
                .limit(pageRequestDto.size())
                .fetch();
    }

    private LocalDateTime findLastStartTime(final Long cursor) {
        if (cursor == null) {
            return null;
        }
        return jpaQueryFactory
                .select(game.startTime)
                .from(game)
                .where(game.id.eq(cursor))
                .fetchFirst();
    }

}
