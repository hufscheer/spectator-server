package com.sports.server.game.application;

import static java.util.stream.Collectors.groupingBy;

import com.sports.server.game.domain.GameTeam;
import com.sports.server.game.domain.GameTeamPlayer;
import com.sports.server.game.domain.GameTeamPlayerRepository;
import com.sports.server.game.dto.response.GameLineupResponse;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GameTeamPlayerService {

    private final GameTeamPlayerRepository gameTeamPlayerRepository;

    public List<GameLineupResponse> getLineup(final Long gameId) {
        Map<GameTeam, List<GameTeamPlayer>> groupByTeam = gameTeamPlayerRepository.findPlayersByGameId(gameId)
                .stream()
                .collect(groupingBy(GameTeamPlayer::getGameTeam));

        return IntStream.range(0, groupByTeam.size())
                .boxed()
                .sorted(Comparator.comparingLong(i -> new ArrayList<>(groupByTeam.keySet()).get(i).getId()))
                .map(i -> {
                    GameTeam gameTeam = new ArrayList<>(groupByTeam.keySet()).get(i);
                    List<GameTeamPlayer> players = groupByTeam.get(gameTeam);
                    return new GameLineupResponse(gameTeam, players, i + 1);
                })
                .toList();
    }

}
