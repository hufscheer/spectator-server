package com.sports.server.query.repository;

import com.sports.server.command.player.domain.Player;
import java.util.List;

public interface PlayerDynamicRepository {
    List<Player> findAllByOrganizationId(Long organizationId, Long cursor, Integer size);
}
