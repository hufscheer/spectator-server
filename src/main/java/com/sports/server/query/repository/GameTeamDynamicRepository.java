package com.sports.server.query.repository;

import static com.sports.server.command.game.domain.QGameTeam.gameTeam;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class GameTeamDynamicRepository {

    private final JPAQueryFactory jpaQueryFactory;

    public List<Long> findAllGameTeamIdsByLeagueTeams(final List<Long> leagueTeamIds) {
        BooleanBuilder condition = DynamicBooleanBuilder.builder()
                .and(() -> gameTeam.leagueTeam.id.in(leagueTeamIds)).build();

        return jpaQueryFactory
                .selectFrom(gameTeam)
                .select(gameTeam.game.id)
                .where(condition)
                .stream().toList();
    }

}
