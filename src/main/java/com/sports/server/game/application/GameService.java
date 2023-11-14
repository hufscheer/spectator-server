package com.sports.server.game.application;

import com.sports.server.common.application.EntityUtils;
import com.sports.server.game.domain.Game;
import com.sports.server.game.domain.GameRepository;
import com.sports.server.game.dto.response.GameDetailResponseDto;
import com.sports.server.game.dto.response.GameResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GameService {

    private final GameRepository gameRepository;
    private final EntityUtils entityUtils;

    public GameDetailResponseDto getOneGame(final Long gameId) {
        Game game = entityUtils.getEntity(gameId, Game.class);
        return new GameDetailResponseDto(game);
    }

    public List<GameResponseDto> getAllGames() {
        return gameRepository.findAll().stream().map(GameResponseDto::new)
                .toList();
    }

}
