package com.sports.server.query.application;

import com.sports.server.query.dto.response.TeamDto;
import com.sports.server.query.repository.TeamQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TeamQueryService {

    private final TeamQueryRepository teamQueryRepository;

    public List<TeamDto> findAllTeams() {
        return teamQueryRepository.findAll()
                .stream()
                .map(TeamDto::new)
                .toList();
    }
}
