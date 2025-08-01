package com.sports.server.query.repository;

import static com.sports.server.command.game.domain.QGame.game;
import static com.sports.server.command.game.domain.QGameTeam.gameTeam;
import static com.sports.server.command.team.domain.QTeam.team;
import static com.sports.server.command.league.domain.QLeagueTeam.leagueTeam;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sports.server.command.league.domain.League;
import com.sports.server.command.league.domain.Round;
import java.util.List;

import com.sports.server.command.team.domain.Team;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class LeagueTeamDynamicRepository {

    private final JPAQueryFactory jpaQueryFactory;

    public List<Team> findByLeagueAndRound(final League leagueParam, Integer roundNumber) {
        DynamicBooleanBuilder booleanBuilder = DynamicBooleanBuilder.builder()
                .and(() -> leagueTeam.league.eq(leagueParam));

        if (Round.isValidNumber(roundNumber)) {
            Round round = Round.from(roundNumber);
            booleanBuilder.and(() -> game.round.eq(round));
        }

        return jpaQueryFactory
                .selectDistinct(team)
                .from(team)
                .join(leagueTeam).on(leagueTeam.team.eq(team))
                .leftJoin(gameTeam).on(gameTeam.team.eq(team))
                .leftJoin(game).on(gameTeam.game.eq(game))
                .where(booleanBuilder.build())
                .orderBy(team.name.asc())
                .fetch();
    }
}