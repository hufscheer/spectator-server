package com.sports.server.game.application;

import static java.util.stream.Collectors.groupingBy;

import com.sports.server.common.application.EntityUtils;
import com.sports.server.game.domain.Game;
import com.sports.server.game.domain.GameDynamicRepository;
import com.sports.server.game.domain.GameRepository;
import com.sports.server.game.domain.GameState;
import com.sports.server.game.domain.GameTeam;
import com.sports.server.game.domain.GameTeamRepository;
import com.sports.server.game.dto.request.GamesQueryRequestDto;
import com.sports.server.game.dto.request.PageRequestDto;
import com.sports.server.game.dto.response.GameDetailResponse;
import com.sports.server.game.dto.response.GameResponseDto;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GameService {

    private final GameRepository gameRepository;
    private final GameTeamRepository gameTeamRepository;
    private final GameDynamicRepository gameDynamicRepository;
    private final EntityUtils entityUtils;

    public GameDetailResponse getGameDetail(final Long gameId) {
        Game game = entityUtils.getEntity(gameId, Game.class);
        List<GameTeam> teams = gameTeamRepository.findAllByGameWithTeam(game);
        return new GameDetailResponse(game, teams);
    }

    public List<GameResponseDto> getAllGames(
            final GamesQueryRequestDto queryRequestDto, final PageRequestDto pageRequest) {

        GameState state = GameState.from(queryRequestDto.getStateValue());

        List<Game> games = gameDynamicRepository.findAllByLeagueAndStateAndSports(queryRequestDto.getLeagueId(), state,
                queryRequestDto.getSportIds(),
                pageRequest);

        List<GameTeam> gameTeams = gameTeamRepository.findAllByGameIds(
                games.stream()
                        .map(Game::getId)
                        .toList()
        );

        Map<Game, List<GameTeam>> groupedByGame = gameTeams.stream()
                .collect(groupingBy(GameTeam::getGame));

        return groupedByGame.keySet()
                .stream()
                .map(game -> new GameResponseDto(game, groupedByGame.get(game), game.getSport()))
                .toList();
    }
}
