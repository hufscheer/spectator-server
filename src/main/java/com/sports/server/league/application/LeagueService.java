package com.sports.server.league.application;

import com.sports.server.league.domain.LeagueRepository;
import com.sports.server.league.dto.response.LeagueResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class LeagueService {

    private final LeagueRepository leagueRepository;

    public List<LeagueResponse> findAll() {
        return leagueRepository.findAll()
                .stream()
                .map(league -> new LeagueResponse(league.getName()))
                .toList();
    }
}
