package com.sports.server.query.repository;

import static com.sports.server.command.game.domain.QGameTeam.gameTeam;

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
                .selectFrom(gameTeam)
                .select(gameTeam.game.id)
                .where(gameTeam.team.id.in(leagueTeamIds))
                .fetch();
    }
}
