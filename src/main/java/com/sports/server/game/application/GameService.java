package com.sports.server.game.application;

import com.sports.server.common.application.EntityUtils;
import com.sports.server.game.domain.Game;
import com.sports.server.game.domain.GameRepository;
import com.sports.server.game.domain.GameTeam;
import com.sports.server.game.domain.GameTeamRepository;
import com.sports.server.game.dto.response.GameDetailResponse;
import com.sports.server.game.dto.response.GameResponseDto;
import com.sports.server.league.domain.League;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GameService {

    private final GameRepository gameRepository;
    private final GameTeamRepository gameTeamRepository;
    private final EntityUtils entityUtils;

    public GameDetailResponse getGameDetail(final Long gameId) {
        Game game = entityUtils.getEntity(gameId, Game.class);
        List<GameTeam> teams = gameTeamRepository.findAllByGameWithTeam(game);
        return new GameDetailResponse(game, teams);
    }

    public List<GameResponseDto> getAllGamesWithLeague(final Long leagueId) {
        League league = entityUtils.getEntity(leagueId, League.class);
        List<Game> games = gameRepository.findAllByLeague(league);
        return games.stream()
                .map(game -> new GameResponseDto(game, gameTeamRepository.findAllByGameWithTeam(game), game.getSport()))
                .toList();
    }
}
