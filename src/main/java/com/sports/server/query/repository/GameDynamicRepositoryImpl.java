package com.sports.server.query.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sports.server.command.game.domain.Game;
import com.sports.server.query.dto.request.GamesQueryRequestDto;
import com.sports.server.common.dto.PageRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.sports.server.command.game.domain.QGame.game;
import static com.sports.server.command.sport.domain.QSport.sport;

@Repository
@RequiredArgsConstructor
public class GameDynamicRepositoryImpl implements GameDynamicRepository {

    private final JPAQueryFactory jpaQueryFactory;
    private final GamesQueryConditionMapper conditionMapper;

    @Override
    public List<Game> findAllByLeagueAndStateAndSports(final GamesQueryRequestDto gameQueryRequestDto,
                                                       final PageRequestDto pageRequestDto) {
        return jpaQueryFactory
                .selectFrom(game)
                .join(game.sport, sport).fetchJoin()
                .where(conditionMapper.mapBooleanCondition(gameQueryRequestDto, pageRequestDto))
                .orderBy(conditionMapper.mapOrderCondition(gameQueryRequestDto))
                .limit(pageRequestDto.size())
                .fetch();
    }

}
