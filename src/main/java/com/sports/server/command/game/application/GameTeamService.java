package com.sports.server.command.game.application;

import com.sports.server.command.game.domain.Game;
import com.sports.server.command.game.domain.GameTeam;
import com.sports.server.command.game.domain.GameTeamRepository;
import com.sports.server.command.game.dto.CheerCountUpdateRequest;
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


    public void updateCheerCount(final Long gameId, final CheerCountUpdateRequest cheerCountUpdateRequest) {
        Game game = entityUtils.getEntity(gameId, Game.class);
        GameTeam gameTeam = entityUtils.getEntity(cheerCountUpdateRequest.gameTeamId(), GameTeam.class);
        gameTeam.validateCheerCountOfGameTeam(cheerCountUpdateRequest.cheerCount());
        validateGameTeam(gameTeam, game);
        gameTeamRepository.updateCheerCount(cheerCountUpdateRequest.gameTeamId(), cheerCountUpdateRequest.cheerCount());
    }

    private void validateGameTeam(final GameTeam gameTeam, final Game game) {
        if (!gameTeam.matchGame(game)) {
            throw new CustomException(HttpStatus.NOT_FOUND, GameErrorMessages.GAME_TEAM_NOT_PARTICIPANT_EXCEPTION);
        }
    }
}
