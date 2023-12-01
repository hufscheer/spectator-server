package com.sports.server.game.infra;

import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sports.server.common.dto.PageRequestDto;
import com.sports.server.game.domain.Game;
import com.sports.server.game.domain.GameDynamicRepository;
import com.sports.server.game.domain.GameState;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static com.sports.server.game.domain.QGame.game;
import static com.sports.server.sport.domain.QSport.sport;

@Repository
@RequiredArgsConstructor
public class GameDynamicRepositoryImpl implements GameDynamicRepository {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<Game> findAllByLeagueAndStateAndSports(final Long leagueId, final GameState state,
                                                       final List<Long> sportIds, final PageRequestDto pageRequestDto) {
        DynamicBooleanBuilder booleanBuilder = DynamicBooleanBuilder.builder();
        String cursorValue = getCursorValue(pageRequestDto.cursor());
        JPAQuery<Game> gameJPAQuery = jpaQueryFactory
                .selectFrom(game)
                .join(game.sport, sport).fetchJoin()
                .where(booleanBuilder
                        .and(() -> game.startTime.stringValue().concat(game.id.stringValue()).gt(cursorValue))
                        .and(() -> game.league.id.eq(leagueId))
                        .and(() -> game.state.eq(state))
                        .and(() -> game.sport.id.in(sportIds))
                        .build()
                ).limit(pageRequestDto.size());

        if (state.equals(GameState.FINISHED)) {
            return gameJPAQuery
                    .orderBy(game.startTime.stringValue().concat(game.id.stringValue()).desc())
                    .fetch();
        }

        return gameJPAQuery
                .orderBy(game.startTime.stringValue().concat(game.id.stringValue()).asc())
                .fetch();
    }

    private String getCursorValue(Long cursor) {
        LocalDateTime lastStartTime = findLastStartTime(cursor);
        if (lastStartTime == null) {
            return null;
        }
        return lastStartTime.format(FORMATTER) + cursor;
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
