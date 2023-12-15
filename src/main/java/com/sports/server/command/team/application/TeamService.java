package com.sports.server.command.team.application;

import com.sports.server.command.team.domain.Team;
import com.sports.server.command.team.domain.TeamRepository;
import com.sports.server.command.team.dto.TeamDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TeamService {

    private final TeamRepository teamRepository;

    public Team findTeamWithId(final Long id) {
        // TODO: 예외 핸들링하기
        return teamRepository.findById(id).orElseThrow(IllegalArgumentException::new);
    }

    public List<TeamDto> findAllTeams() {
        return teamRepository.findAll().stream().map(TeamDto::new).toList();
    }
}
