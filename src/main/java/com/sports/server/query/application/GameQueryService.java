package com.sports.server.query.application;

import com.sports.server.command.game.domain.Game;
import com.sports.server.command.game.domain.GameTeam;
import com.sports.server.query.dto.request.GamesQueryRequestDto;
import com.sports.server.query.dto.response.GameDetailResponse;
import com.sports.server.query.dto.response.GameResponseDto;
import com.sports.server.query.dto.response.VideoResponse;
import com.sports.server.common.application.EntityUtils;
import com.sports.server.common.dto.PageRequestDto;
import com.sports.server.query.repository.GameDynamicRepository;
import com.sports.server.query.repository.GameTeamQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.groupingBy;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GameQueryService {

    private final GameTeamQueryRepository gameTeamQueryRepository;
    private final GameDynamicRepository gameDynamicRepository;
    private final EntityUtils entityUtils;

    public GameDetailResponse getGameDetail(final Long gameId) {
        Game game = entityUtils.getEntity(gameId, Game.class);
        List<GameTeam> teams = gameTeamQueryRepository.findAllByGameWithTeam(game);
        return new GameDetailResponse(game, teams);
    }

    public List<GameResponseDto> getAllGames(final GamesQueryRequestDto queryRequestDto,
                                             final PageRequestDto pageRequest) {

        List<Game> games = gameDynamicRepository.findAllByLeagueAndStateAndSports(queryRequestDto, pageRequest);
        List<GameTeam> gameTeams = gameTeamQueryRepository.findAllByGameIds(
                games.stream()
                        .map(Game::getId)
                        .toList()
        );

        Map<Game, List<GameTeam>> groupedByGame = gameTeams.stream()
                .collect(groupingBy(GameTeam::getGame));

        return games.stream()
                .map(game -> new GameResponseDto(game, groupedByGame.getOrDefault(game, new ArrayList<>())))
                .toList();
    }

    public VideoResponse getVideo(Long gameId) {
        Game game = entityUtils.getEntity(gameId, Game.class);
        return new VideoResponse(game.getVideoId());
    }
}
