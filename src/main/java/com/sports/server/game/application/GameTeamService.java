package com.sports.server.game.application;

import com.sports.server.common.application.EntityUtils;
import com.sports.server.common.exception.CustomException;
import com.sports.server.game.domain.Game;
import com.sports.server.game.domain.GameTeam;
import com.sports.server.game.domain.GameTeamRepository;
import com.sports.server.game.dto.request.GameTeamCheerRequestDto;
import com.sports.server.game.dto.response.GameTeamCheerResponseDto;
import com.sports.server.game.exception.GameErrorMessages;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GameTeamService {

    private final GameTeamRepository gameTeamRepository;
    private final EntityUtils entityUtils;

    public List<GameTeamCheerResponseDto> getCheerCountOfGameTeams(final Long gameId) {
        Game game = entityUtils.getEntity(gameId, Game.class);
        return gameTeamRepository.findAllByGame(game).stream()
                .map(GameTeamCheerResponseDto::new)
                .toList();
    }

    @Transactional
    public void updateCheerCount(final Long gameId, final GameTeamCheerRequestDto cheerRequestDto) {
        Game game = entityUtils.getEntity(gameId, Game.class);
        GameTeam gameTeam = entityUtils.getEntity(cheerRequestDto.gameTeamId(), GameTeam.class);
        validateGameTeam(gameTeam, game);
        gameTeamRepository.updateCheerCount(cheerRequestDto.gameTeamId(), cheerRequestDto.cheerCount());
    }

    private void validateGameTeam(final GameTeam gameTeam, final Game game) {
        if (!gameTeam.getGame().equals(game)) {
            throw new CustomException(HttpStatus.NOT_FOUND, GameErrorMessages.GAME_TEAM_NOT_PARTICIPANT_EXCEPTION);
        }
    }
}
