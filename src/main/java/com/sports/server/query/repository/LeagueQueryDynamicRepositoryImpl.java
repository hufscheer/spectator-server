package com.sports.server.query.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sports.server.command.league.domain.League;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.sports.server.command.league.domain.QLeague.league;

@Repository
@RequiredArgsConstructor
public class LeagueQueryDynamicRepositoryImpl implements LeagueQueryDynamicRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<League> findByYear(Integer year) {
        return queryFactory
                .selectFrom(league)
                .where(DynamicBooleanBuilder.builder()
                        .and(() -> league.startAt.year().eq(year))
                        .build())
                .orderBy(league.startAt.desc(), league.endAt.desc())
                .fetch();
    }

    @Override
    public List<League> findByYearAndName(Integer year, String name) {
        return queryFactory
                .selectFrom(league)
                .where(DynamicBooleanBuilder.builder()
                        .and(() -> league.startAt.year().eq(year))
                        .and(() -> league.name.contains(name))
                        .build())
                .orderBy(league.startAt.desc(), league.endAt.desc())
                .fetch();
    }
}
