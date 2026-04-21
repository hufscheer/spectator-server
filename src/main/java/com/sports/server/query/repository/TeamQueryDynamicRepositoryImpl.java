package com.sports.server.query.repository;

import static com.sports.server.command.game.domain.QGame.game;
import static com.sports.server.command.game.domain.QGameTeam.gameTeam;
import static com.sports.server.command.league.domain.QLeagueTeam.leagueTeam;
import static com.sports.server.command.team.domain.QTeam.team;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sports.server.command.league.domain.League;
import com.sports.server.command.league.domain.LeagueTeam;
import com.sports.server.command.league.domain.Round;
import com.sports.server.command.league.domain.SportType;
import java.util.List;

import com.sports.server.command.team.domain.Team;
import com.sports.server.command.team.domain.Unit;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class TeamQueryDynamicRepositoryImpl implements TeamQueryDynamicRepository {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<LeagueTeam> findByLeagueAndRound(final League league, Integer roundNumber) {
        return jpaQueryFactory
                .selectFrom(leagueTeam)
                .join(leagueTeam.team, team).fetchJoin()
                .where(
                        leagueTeam.league.eq(league),
                        teamsPlayedInRound(league, roundNumber)
                )
                .orderBy(team.name.asc())
                .fetch();
    }

    @Override
    public List<Team> findAllByUnitsAndSportType(final List<Unit> units, final SportType sportType,
                                                  final Long organizationId) {
        return jpaQueryFactory
                .selectFrom(team)
                .where(
                        teamsInUnits(units),
                        teamsWithSportType(sportType),
                        teamsInOrganization(organizationId)
                )
                .orderBy(team.name.asc())
                .fetch();
    }

    private BooleanExpression teamsPlayedInRound(final League league, final Integer roundNumber) {
        if (!Round.isValidNumber(roundNumber)) {
            return null;
        }
        Round round = Round.from(roundNumber);

        return leagueTeam.team.id.in(
                JPAExpressions.select(gameTeam.team.id)
                        .from(gameTeam)
                        .join(gameTeam.game, game)
                        .where(
                                game.league.eq(league),
                                game.round.eq(round)
                        )
        );
    }

    @Override
    public List<Unit> findDistinctUnitsBySportType(final SportType sportType) {
        return jpaQueryFactory
                .select(team.unit).distinct()
                .from(team)
                .where(teamsWithSportType(sportType))
                .fetch();
    }

    private BooleanExpression teamsInUnits(final List<Unit> units) {
        if (units == null || units.isEmpty()) {
            return null;
        }
        return team.unit.in(units);
    }

    private BooleanExpression teamsWithSportType(final SportType sportType) {
        if (sportType == null) {
            return null;
        }
        return team.sportType.eq(sportType);
    }

    private BooleanExpression teamsInOrganization(final Long organizationId) {
        if (organizationId == null) {
            return null;
        }
        return team.organization.id.eq(organizationId);
    }
}
