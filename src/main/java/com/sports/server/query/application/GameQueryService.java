package com.sports.server.query.application;

import com.sports.server.command.game.domain.Game;
import com.sports.server.command.game.domain.GameTeam;
import com.sports.server.command.game.exception.GameErrorMessages;
import com.sports.server.command.team.domain.Team;
import com.sports.server.common.exception.NotFoundException;
import com.sports.server.query.dto.request.GamesQueryRequestDto;
import com.sports.server.query.dto.response.GameDetailResponse;
import com.sports.server.query.dto.response.GameResponseDto;
import com.sports.server.query.dto.response.VideoResponse;
import com.sports.server.common.application.EntityUtils;
import com.sports.server.common.dto.PageRequestDto;
import com.sports.server.query.repository.GameDynamicRepository;
import com.sports.server.query.repository.GameQueryRepository;
import com.sports.server.query.repository.GameTeamQueryRepository;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GameQueryService {

    private final GameTeamQueryRepository gameTeamQueryRepository;
    private final GameDynamicRepository gameDynamicRepository;
    private final GameQueryRepository gameQueryRepository;
    private final EntityUtils entityUtils;

    public GameDetailResponse getGameDetail(final Long gameId) {
        Game game = gameQueryRepository.findGameDetailsById(gameId)
                .orElseThrow(() -> new NotFoundException(GameErrorMessages.GAME_NOT_FOUND_EXCEPTION));
        return new GameDetailResponse(game, game.getGameTeams(), game.getLeague().getName());
    }

    public List<GameDetailResponse> getAllGamesDetailByTeam(final Long teamId) {
        entityUtils.getEntity(teamId, Team.class);

        List<Game> games = gameQueryRepository.findGamesByTeamId(teamId);
        return getGameDetailResponses(games);
    }

    public List<GameResponseDto> getAllGames(final GamesQueryRequestDto queryRequestDto,
                                             final PageRequestDto pageRequest) {
            List<Game> games = gameDynamicRepository.findAllByLeagueAndState(queryRequestDto, pageRequest);

            List<Long> gameIds = games.stream().map(Game::getId).toList();
            List<GameTeam> gameTeams = gameTeamQueryRepository.findAllByGameIds(gameIds);

            Map<Long, List<GameTeam>> teamsByGameId = gameTeams.stream()
                    .collect(groupingBy(gameTeam -> gameTeam.getGame().getId()));

            return games.stream()
                    .map(game -> {
                        List<GameTeam> teams = teamsByGameId.getOrDefault(game.getId(), new ArrayList<>());
                        return new GameResponseDto(game, teams);
                    })
                    .toList();
    }

    public VideoResponse getVideo(Long gameId) {
        Game game = entityUtils.getEntity(gameId, Game.class);
        return new VideoResponse(game.getVideoId());
    }

    public List<GameDetailResponse> getGamesByYearAndMonth(Integer year, Integer month) {
        List<Game> games = gameDynamicRepository.findByYearAndMonth(year, month);
        return getGameDetailResponses(games);
    }

    @NotNull
    private List<GameDetailResponse> getGameDetailResponses(List<Game> games) {
        if (games.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> gameIds = games.stream().map(Game::getId).toList();
        List<GameTeam> allGameTeams = gameTeamQueryRepository.findAllByGameIds(gameIds);

        Map<Long, List<GameTeam>> teamsByGameId = allGameTeams.stream()
                .collect(Collectors.groupingBy(gameTeam -> gameTeam.getGame().getId()));

        return games.stream()
                .map(game -> {
                    List<GameTeam> gameTeams = teamsByGameId.getOrDefault(game.getId(), Collections.emptyList());
                    return new GameDetailResponse(game, gameTeams, game.getLeague().getName());
                })
                .toList();
    }
}
