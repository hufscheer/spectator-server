package com.sports.server.command.game.domain;

import com.sports.server.command.game.dto.request.GamesQueryRequestDto;
import com.sports.server.common.dto.PageRequestDto;

import java.util.List;

public interface GameDynamicRepository {
    List<Game> findAllByLeagueAndStateAndSports(final GamesQueryRequestDto gamesQueryRequestDto,
                                                final PageRequestDto pageRequestDto);
}
