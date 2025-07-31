package com.sports.server.query.application;

import static java.util.Comparator.comparingLong;

import com.sports.server.command.game.domain.Game;
import com.sports.server.command.game.domain.GameTeam;
import com.sports.server.common.application.EntityUtils;
import com.sports.server.query.dto.response.GameTeamCheerResponseDto;
import com.sports.server.query.repository.GameTeamQueryRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GameTeamQueryService {

    private final GameTeamQueryRepository gameTeamQueryRepository;
    private final EntityUtils entityUtils;

    public List<GameTeamCheerResponseDto> getCheerCountOfGameTeams(final Long gameId) {
        Game game = entityUtils.getEntity(gameId, Game.class);

        return gameTeamQueryRepository.findAllByGame(game).stream()
                .sorted(comparingLong(GameTeam::getId))
                .map(GameTeamCheerResponseDto::new)
                .toList();
    }
}
