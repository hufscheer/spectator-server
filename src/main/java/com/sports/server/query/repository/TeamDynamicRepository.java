package com.sports.server.query.repository;

import static com.sports.server.command.game.domain.QGame.game;
import static com.sports.server.command.game.domain.QGameTeam.gameTeam;
import static com.sports.server.command.league.domain.QLeagueTeam.leagueTeam;
import static com.sports.server.command.team.domain.QTeam.team;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sports.server.command.league.domain.League;
import com.sports.server.command.league.domain.Round;
import java.util.List;

import com.sports.server.command.team.domain.Team;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class TeamDynamicRepository {

    private final JPAQueryFactory jpaQueryFactory;

    public List<Team> findByLeagueAndRound(final League league, Integer roundNumber) {

        BooleanBuilder booleanBuilder = new BooleanBuilder();
        booleanBuilder.and(
                JPAExpressions.selectOne()
                        .from(leagueTeam)
                        .where(
                                leagueTeam.team.eq(team),
                                leagueTeam.league.eq(league)
                        ).exists()
        );

        if (Round.isValidNumber(roundNumber)) {
            Round round = Round.from(roundNumber);
            booleanBuilder.and(
                    JPAExpressions.selectOne()
                            .from(game)
                            .join(game.teams, gameTeam)
                            .where(
                                    game.league.eq(league),
                                    gameTeam.team.eq(team),
                                    game.round.eq(round)
                            )
                            .exists()
            );
        }

        return jpaQueryFactory
                .selectFrom(team)
                .where(booleanBuilder)
                .orderBy(team.name.asc())
                .fetch();
    }
}
