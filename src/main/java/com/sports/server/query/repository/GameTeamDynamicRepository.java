package com.sports.server.query.repository;

import static com.sports.server.command.game.domain.QGameTeam.gameTeam;
import static com.sports.server.command.league.domain.QLeagueTeam.leagueTeam;

import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;

import java.util.Collections;
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
                .where(gameTeam.team.id.in(
                        JPAExpressions
                                .select(leagueTeam.team.id)
                                .from(leagueTeam)
                                .where(leagueTeam.id.in(leagueTeamIds))
                ))
                .distinct()
                .fetch();
    }
}
