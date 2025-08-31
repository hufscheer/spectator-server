package com.sports.server.query.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sports.server.command.league.domain.League;
import com.sports.server.common.dto.PageRequestDto;
import com.sports.server.query.dto.request.LeagueQueryRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.sports.server.command.league.domain.QLeague.league;

@Repository
@RequiredArgsConstructor
public class LeagueQueryDynamicRepositoryImpl implements LeagueQueryDynamicRepository {

    private final JPAQueryFactory queryFactory;
    private final LeagueQueryConditionMapper conditionMapper;

    @Override
    public List<League> findLeagues(final LeagueQueryRequestDto requestDto, final PageRequestDto pageRequest) {
        return queryFactory
                .selectFrom(league)
                .where(conditionMapper.mapBooleanCondition(requestDto, pageRequest))
                .orderBy(league.startAt.desc(), league.id.desc())
                .limit(pageRequest.size())
                .fetch();
    }
}
