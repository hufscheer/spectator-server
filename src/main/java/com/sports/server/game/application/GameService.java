package com.sports.server.game.application;

import com.sports.server.common.exception.ExceptionMessages;
import com.sports.server.common.exception.NotFoundException;
import com.sports.server.game.domain.Game;
import com.sports.server.game.domain.GameRepository;
import com.sports.server.game.domain.GameTeam;
import com.sports.server.game.domain.GameTeamRepository;
import com.sports.server.game.dto.response.GameTeamCheerResponseDto;
import com.sports.server.game.dto.response.GameDetailResponseDto;
import com.sports.server.game.dto.response.GameResponseDto;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GameService {

    private final GameRepository gameRepository;
    private final GameTeamRepository gameTeamRepository;

    public GameDetailResponseDto getOneGame(final Long gameId) {
        Game game = findGameWithId(gameId);
        return new GameDetailResponseDto(game);
    }

    private Game findGameWithId(final Long gameId) {
        return gameRepository.findById(gameId)
                .orElseThrow(() -> new NotFoundException(ExceptionMessages.GAME_NOT_FOUND_EXCEPTION));
    }

    public List<GameResponseDto> getAllGames() {
        return gameRepository.findAll().stream().map(GameResponseDto::new)
                .toList();
    }


    public List<GameTeamCheerResponseDto> getCheerCountOfGameTeams(final Long gameId) {
        Game game = findGameWithId(gameId);
        List<GameTeam> gameTeams = gameTeamRepository.findAllByGame(game);
        return gameTeams.stream().map(GameTeamCheerResponseDto::new).toList();
    }
}
