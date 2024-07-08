package com.sports.server.query.repository;

import static com.sports.server.command.game.domain.QGameTeam.gameTeam;
import static com.sports.server.command.leagueteam.domain.QLeagueTeam.leagueTeam;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sports.server.command.league.domain.League;
import com.sports.server.command.leagueteam.domain.LeagueTeam;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class LeagueTeamDynamicRepository {

    private final JPAQueryFactory jpaQueryFactory;

    public List<LeagueTeam> findByLeagueAndRound(final League league, Integer round) {
        return jpaQueryFactory
                .selectFrom(leagueTeam)
                .leftJoin(gameTeam).on(leagueTeam.eq(gameTeam.leagueTeam))
                .where(DynamicBooleanBuilder.builder()
                        .and(() -> leagueTeam.league.eq(league))
                        .and(() -> gameTeam.game.round.eq(round))
                        .build())
                .orderBy(leagueTeam.name.asc())
                .fetch();
    }
}
