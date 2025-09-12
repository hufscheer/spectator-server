package com.sports.server.query.repository;

import com.sports.server.command.league.domain.League;
import com.sports.server.query.dto.request.LeagueQueryRequestDto;

import java.util.List;

public interface LeagueQueryDynamicRepository {

    List<League> findLeagues(LeagueQueryRequestDto requestDto);
}
