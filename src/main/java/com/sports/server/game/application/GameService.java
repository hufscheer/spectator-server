package com.sports.server.game.application;

import com.sports.server.common.exception.ExceptionMessages;
import com.sports.server.common.exception.NotFoundException;
import com.sports.server.game.domain.Game;
import com.sports.server.game.domain.GameRepository;
import com.sports.server.game.dto.response.GameDetailResponseDto;
import com.sports.server.game.dto.response.GameResponseDto;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GameService {

    private final GameRepository gameRepository;

    public GameDetailResponseDto getOneGame(final Long gameId) {
        Game game = findGameWithId(gameId);
        return new GameDetailResponseDto(game);
    }

    public Game findGameWithId(final Long gameId) {
        return gameRepository.findById(gameId)
                .orElseThrow(() -> new NotFoundException(ExceptionMessages.GAME_NOT_FOUND_EXCEPTION));
    }

    public List<GameResponseDto> getAllGames() {
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        return gameRepository.findAllByStartTimeIsAfterOrderByStartTime(sevenDaysAgo).stream().map(GameResponseDto::new)
                .toList();
    }
}
