package com.sports.server.query.application;

import com.sports.server.command.team.domain.Team;
import com.sports.server.common.application.EntityUtils;
import com.sports.server.query.dto.response.PlayerResponse;
import com.sports.server.query.dto.response.TeamDetailResponse;
import com.sports.server.query.dto.response.TeamResponse;
import com.sports.server.query.repository.PlayerQueryRepository;
import com.sports.server.query.repository.TeamQueryRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TeamQueryService {

    private final TeamQueryRepository teamQueryRepository;
    private final EntityUtils entityUtils;
    private final PlayerQueryRepository playerQueryRepository;

    public List<TeamResponse> getAllTeams(){
        return teamQueryRepository.findAll().stream()
                .map(TeamResponse::new)
                .toList();
    }

    public TeamDetailResponse getTeamDetail(Long teamId){
        Team team = entityUtils.getEntity(teamId, Team.class);
        List<PlayerResponse> teamPlayers = playerQueryRepository.findPlayersByTeamId(teamId);
        return new TeamDetailResponse(team, teamPlayers);
    }
}
