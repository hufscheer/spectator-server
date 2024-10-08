package com.sports.server.query.application;

import static java.util.stream.Collectors.groupingBy;

import com.sports.server.command.game.domain.GameTeam;
import com.sports.server.command.game.domain.LineupPlayer;
import com.sports.server.command.game.domain.LineupPlayerState;
import com.sports.server.query.dto.response.LineupPlayerResponse;
import com.sports.server.query.dto.response.LineupPlayerResponseSeparated;
import com.sports.server.query.repository.LineupPlayerQueryRepository;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LineupPlayerQueryService {

    private final LineupPlayerQueryRepository lineupPlayerQueryRepository;

    public List<LineupPlayerResponseSeparated> getLineup(final Long gameId) {
        Map<GameTeam, List<LineupPlayer>> groupByTeam = lineupPlayerQueryRepository.findPlayersByGameId(gameId)
                .stream()
                .collect(groupingBy(LineupPlayer::getGameTeam));

        List<GameTeam> gameTeams = groupByTeam.keySet().stream().toList();

        return gameTeams.stream()
                .sorted(Comparator.comparingLong(GameTeam::getId))
                .map(gameTeam -> new LineupPlayerResponseSeparated(
                        gameTeam, groupByTeam.getOrDefault(gameTeam, new ArrayList<>())))
                .toList();
    }

    public List<LineupPlayerResponse> getPlayingLineup(final Long gameId) {
        Map<GameTeam, List<LineupPlayer>> groupByTeam = lineupPlayerQueryRepository.findPlayingPlayersByGameId(gameId)
                .stream()
                .collect(groupingBy(LineupPlayer::getGameTeam));

        List<GameTeam> gameTeams = groupByTeam.keySet().stream().toList();

        return gameTeams.stream()
                .sorted(Comparator.comparingLong(GameTeam::getId))
                .map(gameTeam -> new LineupPlayerResponse(gameTeam,
                        groupByTeam.getOrDefault(gameTeam, new ArrayList<>())))
                .toList();
    }
}
