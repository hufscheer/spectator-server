package com.sports.server.game.application;

import com.sports.server.common.exception.CustomException;
import com.sports.server.game.domain.Game;
import com.sports.server.game.domain.GameTeam;
import com.sports.server.game.domain.GameTeamRepository;
import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GameTeamServiceUtils {

    private final GameTeamRepository gameTeamRepository;

    public int calculateOrderOfGameTeam(final Game game, final Long gameTeamId) {
        List<GameTeam> gameTeams = gameTeamRepository.findAllByGame(game);

        gameTeams.sort(Comparator.comparing(GameTeam::getId));

        return IntStream.range(0, gameTeams.size())
                .filter(i -> gameTeams.get(i).getId().equals(gameTeamId))
                .findFirst().orElseThrow(CustomException::new) + 1;
    }
}
