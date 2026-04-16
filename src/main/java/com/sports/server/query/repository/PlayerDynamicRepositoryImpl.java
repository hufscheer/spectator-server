package com.sports.server.query.repository;

import static com.sports.server.command.player.domain.QPlayer.player;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sports.server.command.player.domain.Player;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PlayerDynamicRepositoryImpl implements PlayerDynamicRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Player> findAllByOrganizationId(Long organizationId, Long cursor, Integer size) {
        return queryFactory.selectFrom(player)
                .where(
                        player.organization.id.eq(organizationId),
                        getCursorCondition(cursor)
                )
                .orderBy(player.id.desc())
                .limit(size)
                .fetch();
    }

    private BooleanExpression getCursorCondition(Long cursor) {
        if (cursor == null) {
            return null;
        }
        return player.id.lt(cursor);
    }
}
