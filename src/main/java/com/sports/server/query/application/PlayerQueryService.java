package com.sports.server.query.application;

import com.sports.server.command.player.domain.Player;
import com.sports.server.command.team.domain.PlayerGoalCount;
import com.sports.server.command.team.domain.TeamPlayer;
import com.sports.server.command.team.domain.TeamPlayerRepository;
import com.sports.server.common.application.EntityUtils;
import com.sports.server.query.dto.response.PlayerResponse;
import com.sports.server.query.dto.response.TeamResponse;
import com.sports.server.query.repository.PlayerQueryRepository;
import com.sports.server.query.repository.TimelineQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PlayerQueryService {

    private final PlayerQueryRepository playerQueryRepository;
    private final EntityUtils entityUtils;
    private final TimelineQueryRepository timelineQueryRepository;
    private final TeamPlayerRepository teamPlayerRepository;
    private final PlayerInfoProvider playerInfoProvider;

    public List<PlayerResponse> getAllPlayers(){
        List<Player> players = playerQueryRepository.findAll();
        if (players.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> playerIds = players.stream().map(Player::getId).toList();

        List<TeamPlayer> allTeamPlayers = teamPlayerRepository.findAllByPlayerIds(playerIds);
        Map<Long, List<TeamResponse>> playerTeamsMap = allTeamPlayers.stream()
                .collect(Collectors.groupingBy(
                        tp -> tp.getPlayer().getId(),
                        Collectors.mapping(
                                tp -> new TeamResponse(tp.getTeam()),
                                Collectors.toList()
                        )
                ));

        Map<Long, Integer> playerTotalGoalCountInfo = playerInfoProvider.getPlayersTotalGoalInfo(playerIds);
        return players.stream()
                .map(player -> {
                    Long playerId = player.getId();
                    List<TeamResponse> teams = playerTeamsMap.getOrDefault(playerId, Collections.emptyList());
                    int totalGoals = playerTotalGoalCountInfo.getOrDefault(playerId, 0);
                    return PlayerResponse.of(player, totalGoals, teams);
                })
                .toList();
    }

    public PlayerResponse getPlayerDetail(Long playerId){
        Player player = entityUtils.getEntity(playerId, Player.class);

        List<TeamPlayer> teamPlayers = teamPlayerRepository.findAllByPlayerId(playerId);
        List<TeamResponse> teams = teamPlayers.stream()
                .map(teamPlayer -> new TeamResponse(teamPlayer.getTeam()))
                .toList();

        return PlayerResponse.of(player, countPlayerTotalGoal(playerId), teams);
    }

//    public Map<Long, Integer> getPlayersTotalGoalInfo(List<Long> playerIds){
//        if (playerIds == null || playerIds.isEmpty()) {
//            return Collections.emptyMap();
//        }
//
//        List<PlayerGoalCount> results = timelineQueryRepository.countTotalGoalsByPlayerId(playerIds);
//        return results.stream()
//                .collect(Collectors.toMap(
//                        PlayerGoalCount::playerId,
//                        dto -> dto.playerTotalGoalCount().intValue()
//                ));
//    }

    private int countPlayerTotalGoal(Long playerId){
        return timelineQueryRepository.countTotalGoalsByPlayerId(playerId);
    }
}
