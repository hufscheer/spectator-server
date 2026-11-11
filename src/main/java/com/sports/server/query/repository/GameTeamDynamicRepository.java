package com.sports.server.query.repository;

import static com.sports.server.command.game.domain.QGameTeam.gameTeam;
import static com.sports.server.command.league.domain.QLeagueTeam.leagueTeam;

import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class GameTeamDynamicRepository {

    private final JPAQueryFactory jpaQueryFactory;

    public List<Long> findAllByLeagueTeamIds(final List<Long> leagueTeamIds) {
        return jpaQueryFactory
                .select(gameTeam.game.id)
                .from(gameTeam)
                .where(Expressions.numberTemplate(Long.class, "fk({0})", gameTeam.team).in(
                        JPAExpressions
                                .select(Expressions.numberTemplate(Long.class, "cast(fk({0}) as long)", leagueTeam.team))
                                .from(leagueTeam)
                                .where(leagueTeam.id.in(leagueTeamIds))
                ))
                .distinct()
                .fetch();
    }
}
