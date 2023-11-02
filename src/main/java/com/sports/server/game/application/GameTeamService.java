package com.sports.server.game.application;

import com.sports.server.common.exception.ExceptionMessages;
import com.sports.server.common.exception.NotFoundException;
import com.sports.server.game.domain.GameTeam;
import com.sports.server.game.domain.GameTeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GameTeamService {

    private final GameTeamRepository gameTeamRepository;

    public GameTeam findGameTeamWithId(final Long gameTeamId) {
        return gameTeamRepository.findById(gameTeamId)
                .orElseThrow(() -> new NotFoundException(ExceptionMessages.GAME_TEAM_NOT_FOUND_EXCEPTION));
    }
}
