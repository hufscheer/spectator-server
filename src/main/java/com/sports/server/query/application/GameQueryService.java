package com.sports.server.query.application;

import com.sports.server.command.game.domain.Game;
import com.sports.server.command.game.domain.GameTeam;
import com.sports.server.command.game.exception.GameErrorMessages;
import com.sports.server.command.league.domain.League;
import com.sports.server.command.team.domain.Team;
import com.sports.server.common.exception.NotFoundException;
import com.sports.server.query.dto.request.GamesQueryRequestDto;
import com.sports.server.query.dto.response.GameDetailResponse;
import com.sports.server.query.dto.response.GameResponseDto;
import com.sports.server.query.dto.response.LeagueWithGamesResponse;
import com.sports.server.query.dto.response.VideoResponse;
import com.sports.server.common.application.EntityUtils;
import com.sports.server.common.dto.CursorPageResponse;
import com.sports.server.common.dto.PageRequestDto;
import com.sports.server.query.repository.GameDynamicRepository;
import com.sports.server.query.repository.GameQueryRepository;
import com.sports.server.query.repository.GameTeamQueryRepository;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
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
        return new GameDetailResponse(game, game.getGameTeams());
    }

    public List<GameDetailResponse> getAllGamesDetailByTeam(final Long teamId) {
        entityUtils.getEntity(teamId, Team.class);

        List<Game> games = gameQueryRepository.findGamesByTeamId(teamId);
        return games.stream()
                .map(game -> new GameDetailResponse(game, game.getGameTeams()))
                .toList();
    }

    public CursorPageResponse<LeagueWithGamesResponse> getAllGames(final GamesQueryRequestDto queryRequestDto,
                                             final PageRequestDto pageRequest) {
        List<Game> games = gameDynamicRepository.findAllByLeagueAndState(queryRequestDto, pageRequest);

        boolean hasNext = games.size() > pageRequest.size();
        List<Game> sliced = hasNext ? games.subList(0, pageRequest.size()) : games;
        Long nextCursor = hasNext ? sliced.get(sliced.size() - 1).getId() : null;

        if (sliced.isEmpty()) {
            return new CursorPageResponse<>(Collections.emptyList(), null, false);
        }

        List<Long> gameIds = sliced.stream().map(Game::getId).toList();
        List<GameTeam> allGameTeams = gameTeamQueryRepository.findAllByGameIds(gameIds);
        Map<Long, List<GameTeam>> teamsByGameId = allGameTeams.stream()
                .collect(groupingBy(gameTeam -> gameTeam.getGame().getId()));

        Map<League, List<Game>> gamesByLeague = sliced.stream()
                .collect(Collectors.groupingBy(Game::getLeague, LinkedHashMap::new, Collectors.toList()));

        List<LeagueWithGamesResponse> content = gamesByLeague.entrySet().stream()
                .map(entry -> {
                    League league = entry.getKey();
                    List<GameResponseDto> gameResponses = entry.getValue().stream()
                            .map(game -> new GameResponseDto(game, teamsByGameId.getOrDefault(game.getId(), Collections.emptyList())))
                            .toList();
                    return new LeagueWithGamesResponse(league.getId(), league.getName(), gameResponses);
                })
                .toList();

        return new CursorPageResponse<>(content, nextCursor, hasNext);
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
                    return new GameDetailResponse(game, gameTeams);
                })
                .toList();
    }
}
