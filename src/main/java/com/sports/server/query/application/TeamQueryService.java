package com.sports.server.query.application;

import com.sports.server.command.player.domain.Player;
import com.sports.server.command.team.domain.Team;
import com.sports.server.command.team.domain.TeamPlayer;
import com.sports.server.command.team.domain.TeamPlayerRepository;
import com.sports.server.common.application.EntityUtils;
import com.sports.server.query.dto.response.PlayerResponse;
import com.sports.server.query.dto.response.TeamDetailResponse;
import com.sports.server.query.dto.response.TeamResponse;
import com.sports.server.query.repository.TeamQueryRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TeamQueryService {

    private final TeamQueryRepository teamQueryRepository;
    private final TeamPlayerRepository teamPlayerRepository;
    private final EntityUtils entityUtils;
    private final PlayerInfoProvider playerInfoProvider;

    public List<TeamResponse> getAllTeams(){
        return teamQueryRepository.findAll().stream()
                .map(TeamResponse::new)
                .toList();
    }

    public TeamDetailResponse getTeamDetail(Long teamId){
        Team team = entityUtils.getEntity(teamId, Team.class);

        List<TeamPlayer> teamPlayers = teamQueryRepository.findAllTeamPlayer(teamId);
        List<Long> playerIds = teamPlayerRepository.findPlayerIdsByTeamId(teamId);

        Map<Long, Integer> playerTotalGoalCountInfo = playerInfoProvider.getPlayersTotalGoalInfo(playerIds);
        List<PlayerResponse> playerResponses = teamPlayers.stream()
                .map(tp -> {
                    Player player = tp.getPlayer();
                    int totalGoalCount = playerTotalGoalCountInfo.getOrDefault(player.getId(), 0);
                    return PlayerResponse.of(player, totalGoalCount, null);
                })
                .toList();

        return new TeamDetailResponse(team, playerResponses);
    }
}
