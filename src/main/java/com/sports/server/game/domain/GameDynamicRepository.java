package com.sports.server.game.domain;

import com.sports.server.game.dto.request.PageRequestDto;
import java.util.List;

public interface GameDynamicRepository {
    List<Game> findAllByLeagueAndStateAndSports(final Long leagueId, final GameState state, final List<Long> sportIds,
                                                final
                                                PageRequestDto pageRequestDto);
}
