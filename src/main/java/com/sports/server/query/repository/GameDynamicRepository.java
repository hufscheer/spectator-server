package com.sports.server.query.repository;

import com.sports.server.command.game.domain.Game;
import com.sports.server.query.dto.request.GamesQueryRequestDto;
import com.sports.server.common.dto.PageRequestDto;

import java.util.List;

public interface GameDynamicRepository {
    List<Game> findAllByLeagueAndState(final GamesQueryRequestDto gamesQueryRequestDto,
                                       final PageRequestDto pageRequestDto);
}
