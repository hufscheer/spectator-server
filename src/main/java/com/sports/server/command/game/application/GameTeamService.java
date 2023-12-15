package com.sports.server.command.game.application;

import com.sports.server.command.game.domain.Game;
import com.sports.server.command.game.domain.GameTeam;
import com.sports.server.command.game.domain.GameTeamRepository;
import com.sports.server.command.game.dto.request.GameTeamCheerRequestDto;
import com.sports.server.command.game.exception.GameErrorMessages;
import com.sports.server.common.application.EntityUtils;
import com.sports.server.common.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class GameTeamService {

    private final GameTeamRepository gameTeamRepository;
    private final EntityUtils entityUtils;


    public void updateCheerCount(final Long gameId, final GameTeamCheerRequestDto cheerRequestDto) {
        Game game = entityUtils.getEntity(gameId, Game.class);
        GameTeam gameTeam = entityUtils.getEntity(cheerRequestDto.gameTeamId(), GameTeam.class);
        validateGameTeam(gameTeam, game);
        gameTeamRepository.updateCheerCount(cheerRequestDto.gameTeamId(), cheerRequestDto.cheerCount());
    }

    private void validateGameTeam(final GameTeam gameTeam, final Game game) {
        if (!gameTeam.matchGame(game)) {
            throw new CustomException(HttpStatus.NOT_FOUND, GameErrorMessages.GAME_TEAM_NOT_PARTICIPANT_EXCEPTION);
        }
    }
}
