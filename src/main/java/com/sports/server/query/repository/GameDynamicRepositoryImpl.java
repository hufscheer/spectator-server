package com.sports.server.query.repository;

import static com.sports.server.command.game.domain.QGame.game;
import static com.sports.server.command.league.domain.QLeague.league;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sports.server.command.game.domain.Game;
import com.sports.server.common.dto.PageRequestDto;
import com.sports.server.query.dto.request.GamesQueryRequestDto;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class GameDynamicRepositoryImpl implements GameDynamicRepository {

    private final JPAQueryFactory jpaQueryFactory;
    private final GamesQueryConditionMapper conditionMapper;

    @Override
    public List<Game> findAllByLeagueAndState(final GamesQueryRequestDto gameQueryRequestDto,
                                                       final PageRequestDto pageRequestDto) {
        return jpaQueryFactory
                .selectFrom(game)
                .join(game.league, league).fetchJoin()
                .where(conditionMapper.mapBooleanCondition(gameQueryRequestDto, pageRequestDto))
                .orderBy(game.startTime.desc(), game.id.desc())
                .limit(pageRequestDto.size())
                .fetch();
    }

    @Override
    public List<Game> findByYearAndMonth(Integer year, Integer month) {
        return jpaQueryFactory
                .selectFrom(game)
                .where(DynamicBooleanBuilder.builder()
                        .and(() -> game.startTime.year().eq(year))
                        .and(() -> game.startTime.month().eq(month))
                        .build())
                .orderBy(game.startTime.asc(), game.id.asc())
                .fetch();
    }

}
