package com.sports.server.game.domain;

import com.sports.server.common.dto.PageRequestDto;
import com.sports.server.game.dto.request.GamesQueryRequestDto;

import java.util.List;

public interface GameDynamicRepository {
    List<Game> findAllByLeagueAndStateAndSports(final GamesQueryRequestDto gamesQueryRequestDto,
                                                final PageRequestDto pageRequestDto);
}
