package com.sports.server.query.application;

import com.sports.server.command.player.domain.Player;
import com.sports.server.common.application.EntityUtils;
import com.sports.server.query.dto.response.PlayerResponse;
import com.sports.server.query.repository.PlayerQueryRepository;
import com.sports.server.query.repository.TimelineQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PlayerQueryService {

    private static final int PLAYER_ID_IDX = 0;
    private static final int PLAYER_TOTAL_GOAL_COUNT_IDX = 1;

    private final PlayerQueryRepository playerQueryRepository;
    private final EntityUtils entityUtils;
    private final TimelineQueryRepository timelineQueryRepository;

    public List<PlayerResponse> getAllPlayers(){
        List<Player> players = playerQueryRepository.findAll();
        return players.stream()
                .map(player -> {
                    int totalGoals = countPlayerTotalGoal(player.getId());
                    return PlayerResponse.of(player, totalGoals);
                })
                .toList();
    }

    public PlayerResponse findPlayer(Long playerId){
        Player player = entityUtils.getEntity(playerId, Player.class);
        return PlayerResponse.of(player, countPlayerTotalGoal(playerId));
    }

    public Map<Long, Integer> getPlayersTotalGoalInfo(List<Long> playerIds){
        Map<Long, Integer> playerTotalGoalCountInfo = new HashMap<>();
        if (!playerIds.isEmpty()) {
            for (Object[] result : timelineQueryRepository.countTotalGoalsByPlayerId(playerIds)) {
                playerTotalGoalCountInfo.put(
                        (Long) result[PLAYER_ID_IDX],
                        ((Long) result[PLAYER_TOTAL_GOAL_COUNT_IDX]).intValue());
            }
        }
        return playerTotalGoalCountInfo;
    }

    private int countPlayerTotalGoal(Long playerId){
        return timelineQueryRepository.countTotalGoalsByPlayerId(playerId);
    }
}
