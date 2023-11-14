package com.sports.server.game.application;

import com.sports.server.game.domain.Game;
import com.sports.server.game.domain.GameRepository;
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
    private final GameServiceUtils gameServiceUtils;

    public GameDetailResponseDto getOneGame(final Long gameId) {
        Game game = gameServiceUtils.findGameWithId(gameId);
        return new GameDetailResponseDto(game);
    }

    public List<GameResponseDto> getAllGames() {
        return gameRepository.findAll().stream().map(GameResponseDto::new)
                .toList();
    }

}
