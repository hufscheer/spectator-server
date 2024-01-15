package com.sports.server.query.application;

import static java.util.stream.Collectors.groupingBy;

import com.sports.server.command.game.domain.LineupPlayer;
import com.sports.server.query.repository.LineupPlayerQueryRepository;
import com.sports.server.command.game.domain.GameTeam;
import com.sports.server.query.dto.response.LineupPlayerResponse;
import java.util.ArrayList;
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
    private final GameTeamServiceUtils gameTeamServiceUtils;

    public List<LineupPlayerResponse> getLineup(final Long gameId) {
        Map<GameTeam, List<LineupPlayer>> groupByTeam = lineupPlayerQueryRepository.findPlayersByGameId(gameId)
                .stream()
                .collect(groupingBy(LineupPlayer::getGameTeam));

        List<GameTeam> gameTeams = groupByTeam.keySet().stream().toList();

        return gameTeams.stream()
                .map(gameTeam -> new LineupPlayerResponse(gameTeam, groupByTeam.getOrDefault(gameTeam, new ArrayList<>()),
                        gameTeamServiceUtils.calculateOrderOfGameTeam(gameTeams, gameTeam)))
                .toList();
    }
}
