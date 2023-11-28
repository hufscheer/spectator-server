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
        List<GameTeam> gameTeams = findSortedGameTeamsByGame(game);
        return getOrderOfTeam(gameTeamId, gameTeams);
    }

    public int calculateOrderOfGameTeam(final Game game, final GameTeam gameTeam) {
        List<GameTeam> gameTeams = findSortedGameTeamsByGame(game);
        return getOrderOfTeam(gameTeam, gameTeams);
    }

    public int calculateOrderOfGameTeam(final List<GameTeam> gameTeams, final GameTeam gameTeam) {
        List<GameTeam> sortedGameTeams = sortGameTeams(gameTeams);
        return getOrderOfTeam(gameTeam, sortedGameTeams);

    }

    private List<GameTeam> sortGameTeams(List<GameTeam> gameTeams) {
        return gameTeams.stream()
                .sorted(Comparator.comparingLong(GameTeam::getId))
                .toList();
    }

    private List<GameTeam> findSortedGameTeamsByGame(Game game) {
        return sortGameTeams(gameTeamRepository.findAllByGame(game));
    }

    private int getOrderOfTeam(GameTeam gameTeam, List<GameTeam> gameTeams) {
        return gameTeams.indexOf(gameTeam) + 1;
    }

    private int getOrderOfTeam(Long gameTeamId, List<GameTeam> gameTeams) {
        return IntStream.range(0, gameTeams.size())
                .filter(i -> gameTeams.get(i).getId().equals(gameTeamId))
                .findFirst().orElseThrow(CustomException::new) + 1;
    }
}
