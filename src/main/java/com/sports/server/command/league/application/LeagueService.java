package com.sports.server.command.league.application;

import com.sports.server.command.league.domain.LeagueRepository;
import com.sports.server.command.league.domain.LeagueSportRepository;
import com.sports.server.command.league.dto.response.LeagueResponse;
import com.sports.server.command.league.dto.response.LeagueSportResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class LeagueService {

    private final LeagueRepository leagueRepository;
    private final LeagueSportRepository leagueSportRepository;

    public List<LeagueResponse> findAll() {
        return leagueRepository.findAll()
                .stream()
                .map(LeagueResponse::new)
                .toList();
    }

    public List<LeagueSportResponse> findSportsByLeague(Long leagueId) {
        return leagueSportRepository.findByLeagueId(leagueId)
                .stream()
                .map(LeagueSportResponse::new)
                .toList();
    }
}
