package com.sports.server.team.application;

import com.sports.server.team.TeamRepository;
import com.sports.server.team.domain.Team;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TeamService {

    private final TeamRepository teamRepository;

    public Team findTeamWithId(final Long id) {
        // TODO: 예외 핸들링하기
        return teamRepository.findById(id).orElseThrow(IllegalArgumentException::new);
    }
}
