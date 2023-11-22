package com.sports.server.game.domain;

import java.util.List;

public interface GameDynamicRepository {
    List<Game> findAllByLeagueAndStateAndSports(final Long leagueId, final GameState state, final List<Long> sportIds);
}
