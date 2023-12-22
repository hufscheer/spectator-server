package com.sports.server.query.application;

import com.sports.server.command.game.domain.Game;
import com.sports.server.command.game.domain.GameTeam;
import com.sports.server.query.dto.response.GameTeamCheerResponseDto;
import com.sports.server.common.application.EntityUtils;
import com.sports.server.query.repository.GameTeamQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static java.util.Comparator.comparingLong;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GameTeamQueryService {

    private final GameTeamQueryRepository gameTeamQueryRepository;
    private final GameTeamServiceUtils gameTeamServiceUtils;
    private final EntityUtils entityUtils;

    public List<GameTeamCheerResponseDto> getCheerCountOfGameTeams(final Long gameId) {
        Game game = entityUtils.getEntity(gameId, Game.class);

        return gameTeamQueryRepository.findAllByGame(game).stream()
                .sorted(comparingLong(GameTeam::getId))
                .map(gameTeam -> new GameTeamCheerResponseDto(gameTeam,
                        gameTeamServiceUtils.calculateOrderOfGameTeam(game, gameTeam)))
                .toList();
    }
}
