package com.sports.server.game.application;

import com.sports.server.common.exception.NotFoundException;
import com.sports.server.game.domain.Game;
import com.sports.server.game.domain.GameTeam;
import com.sports.server.game.domain.GameTeamRepository;
import com.sports.server.game.dto.request.GameTeamCheerRequestDto;
import com.sports.server.game.dto.response.GameTeamCheerResponseDto;
import com.sports.server.game.exception.GameErrorMessages;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GameTeamService {

    private final GameTeamRepository gameTeamRepository;
    private final GameServiceUtils gameServiceUtils;

    public List<GameTeamCheerResponseDto> getCheerCountOfGameTeams(final Long gameId) {
        Game game = gameServiceUtils.findGameWithId(gameId);
        List<GameTeam> gameTeams = gameTeamRepository.findAllByGame(game);
        return gameTeams.stream()
                .map(GameTeamCheerResponseDto::new)
                .toList();
    }

    private GameTeam getGameTeamWithId(final Long gameTeamId) {
        return gameTeamRepository.findById(gameTeamId)
                .orElseThrow(() -> new NotFoundException(GameErrorMessages.GAME_TEAM_NOT_FOUND_EXCEPTION));
    }

    @Transactional
    public void updateCheerCount(final Long gameId, final GameTeamCheerRequestDto cheerRequestDto) {
        gameServiceUtils.findGameWithId(gameId);
        getGameTeamWithId(cheerRequestDto.gameTeamId());
        gameTeamRepository.updateCheerCount(cheerRequestDto.gameTeamId(), cheerRequestDto.cheerCount());
    }
}
