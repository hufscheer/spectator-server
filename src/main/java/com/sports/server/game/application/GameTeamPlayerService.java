package com.sports.server.game.application;

import static java.util.stream.Collectors.groupingBy;

import com.sports.server.game.domain.GameTeam;
import com.sports.server.game.domain.GameTeamPlayer;
import com.sports.server.game.domain.GameTeamPlayerRepository;
import com.sports.server.game.dto.response.GameLineupResponse;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GameTeamPlayerService {

    private final GameTeamPlayerRepository gameTeamPlayerRepository;
    private final GameTeamServiceUtils gameTeamServiceUtils;

    public List<GameLineupResponse> getLineup(final Long gameId) {
        Map<GameTeam, List<GameTeamPlayer>> groupByTeam = gameTeamPlayerRepository.findPlayersByGameId(gameId)
                .stream()
                .collect(groupingBy(GameTeamPlayer::getGameTeam));

        List<GameTeam> gameTeams = groupByTeam.keySet().stream().toList();

        return gameTeams.stream()
                .map(gameTeam -> new GameLineupResponse(gameTeam, groupByTeam.get(gameTeam),
                        gameTeamServiceUtils.calculateOrderOfGameTeam(gameTeams, gameTeam)))
                .toList();
    }
}
