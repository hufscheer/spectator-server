package com.sports.server.game.application;

import com.sports.server.game.domain.Game;
import com.sports.server.game.domain.GameTeam;
import com.sports.server.game.domain.GameTeamRepository;
import com.sports.server.game.dto.response.GameTeamCheerResponseDto;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GameTeamService {

    private final GameService gameService;
    private final GameTeamRepository gameTeamRepository;

    public List<GameTeamCheerResponseDto> getCheerCountOfGameTeams(final Long gameId) {
        Game game = gameService.findGameWithId(gameId);
        List<GameTeam> gameTeams = gameTeamRepository.findAllByGame(game);
        return gameTeams.stream()
                .map(GameTeamCheerResponseDto::new)
                .toList();
    }

}
