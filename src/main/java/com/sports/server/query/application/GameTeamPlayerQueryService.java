package com.sports.server.query.application;

import static java.util.stream.Collectors.groupingBy;

import com.sports.server.query.repository.GameTeamPlayerQueryRepository;
import com.sports.server.command.game.domain.GameTeam;
import com.sports.server.command.game.domain.GameTeamPlayer;
import com.sports.server.query.dto.response.GameLineupResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GameTeamPlayerQueryService {

    private final GameTeamPlayerQueryRepository gameTeamPlayerQueryRepository;
    private final GameTeamServiceUtils gameTeamServiceUtils;

    public List<GameLineupResponse> getLineup(final Long gameId) {
        Map<GameTeam, List<GameTeamPlayer>> groupByTeam = gameTeamPlayerQueryRepository.findPlayersByGameId(gameId)
                .stream()
                .collect(groupingBy(GameTeamPlayer::getGameTeam));

        List<GameTeam> gameTeams = groupByTeam.keySet().stream().toList();

        return gameTeams.stream()
                .map(gameTeam -> new GameLineupResponse(gameTeam, groupByTeam.getOrDefault(gameTeam, new ArrayList<>()),
                        gameTeamServiceUtils.calculateOrderOfGameTeam(gameTeams, gameTeam)))
                .toList();
    }
}
