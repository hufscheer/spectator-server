package com.sports.server.query.application;

import com.sports.server.query.repository.LeagueQueryRepository;
import com.sports.server.query.repository.LeagueSportQueryRepository;
import com.sports.server.query.dto.response.LeagueResponse;
import com.sports.server.query.dto.response.LeagueSportResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class LeagueQueryService {

    private final LeagueQueryRepository leagueQueryRepository;
    private final LeagueSportQueryRepository leagueSportQueryRepository;

    public List<LeagueResponse> findAll() {
        return leagueQueryRepository.findAll()
                .stream()
                .map(LeagueResponse::new)
                .toList();
    }

    public List<LeagueSportResponse> findSportsByLeague(Long leagueId) {
        return leagueSportQueryRepository.findByLeagueId(leagueId)
                .stream()
                .map(LeagueSportResponse::new)
                .toList();
    }
}
