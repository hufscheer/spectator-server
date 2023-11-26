package com.sports.server.game.domain;

import com.sports.server.common.dto.PageRequestDto;
import java.util.List;

public interface GameDynamicRepository {
    List<Game> findAllByLeagueAndStateAndSports(final Long leagueId, final GameState state, final List<Long> sportIds,
                                                final
                                                PageRequestDto pageRequestDto);
}
