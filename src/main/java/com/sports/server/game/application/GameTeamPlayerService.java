package com.sports.server.game.application;

import com.sports.server.game.domain.GameTeam;
import com.sports.server.game.domain.GameTeamPlayer;
import com.sports.server.game.domain.GameTeamPlayerRepository;
import com.sports.server.game.dto.response.GameLineupResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.groupingBy;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GameTeamPlayerService {

    private final GameTeamPlayerRepository gameTeamPlayerRepository;

    public List<GameLineupResponse> getLineup(final Long gameId) {
        Map<GameTeam, List<GameTeamPlayer>> groupByTeam = gameTeamPlayerRepository.findPlayersByGameId(gameId)
                .stream()
                .collect(groupingBy(GameTeamPlayer::getGameTeam));
        return groupByTeam.keySet()
                .stream()
                .sorted(Comparator.comparingLong(GameTeam::getId))
                .map(gameTeam -> new GameLineupResponse(gameTeam, groupByTeam.get(gameTeam)))
                .toList();
    }
}
