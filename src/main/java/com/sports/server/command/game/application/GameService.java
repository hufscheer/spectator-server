package com.sports.server.command.game.application;

import com.sports.server.auth.exception.AuthorizationErrorMessages;
import com.sports.server.command.game.domain.*;
import com.sports.server.command.game.dto.GameRequest;
import com.sports.server.command.game.exception.GameErrorMessages;
import com.sports.server.command.league.domain.*;
import com.sports.server.command.member.domain.Member;
import com.sports.server.command.player.exception.PlayerErrorMessages;
import com.sports.server.command.team.domain.Team;
import com.sports.server.command.team.domain.TeamPlayer;
import com.sports.server.command.team.domain.TeamPlayerRepository;
import com.sports.server.command.timeline.domain.TimelineRepository;
import com.sports.server.common.application.EntityUtils;
import com.sports.server.common.application.PermissionValidator;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.sports.server.common.exception.CustomException;
import com.sports.server.common.exception.NotFoundException;
import com.sports.server.common.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GameService {

    private final EntityUtils entityUtils;
    private final GameRepository gameRepository;
    private final TimelineRepository timelineRepository;
    private final TeamPlayerRepository teamPlayerRepository;

    @Transactional
    public Long register(final Long leagueId, final GameRequest.Register request, final Member manager) {
        League league = entityUtils.getEntity(leagueId, League.class);
        PermissionValidator.checkPermission(league, manager);
        league.validateRoundWithinLimit(request.round());

        Game game = saveGame(league, manager, request);
        registerGameTeamAndLineup(game, request.team1());
        registerGameTeamAndLineup(game, request.team2());

        return game.getId();
    }

    @Transactional
    public List<Game> updateGameStatusToFinish(LocalDateTime now) {
        LocalDateTime cutoffTime = now.minusHours(5);
        List<Game> games = gameRepository.findGamesOlderThanFiveHours(cutoffTime);
        games.forEach(game -> game.updateState(GameState.FINISHED));
        return games;
    }

    @Transactional
    public void updateGame(Long leagueId, Long gameId, GameRequest.Update request, Member manager) {
        League league = entityUtils.getEntity(leagueId, League.class);
        PermissionValidator.checkPermission(league, manager);
        league.validateRoundWithinLimit(request.round());

        Game game = entityUtils.getEntity(gameId, Game.class);
        game.updateName(request.name());
        game.updateStartTime(request.startTime());
        game.updateVideoId(request.videoId());
        game.updateGameQuarter(request.quarter());
        game.updateState(GameState.from(request.state()));
        game.updateRound(Round.from(request.round()));
    }

    @Transactional
    public void deleteGame(Long leagueId, Long gameId, final Member manager) {
        League league = entityUtils.getEntity(leagueId, League.class);
        PermissionValidator.checkPermission(league, manager);

        Game game = entityUtils.getEntity(gameId, Game.class);
        timelineRepository.deleteByGame(game);
        gameRepository.delete(game);
    }

    @Transactional
    public void deleteGameTeam(final Long gameTeamId, final Member manager) {
        GameTeam gameTeam = entityUtils.getEntity(gameTeamId, GameTeam.class);
        Team team = gameTeam.getTeam();

        Game game = gameTeam.getGame();
        if (!game.isManagedBy(manager)) {
            throw new UnauthorizedException(AuthorizationErrorMessages.PERMISSION_DENIED);
        }

        game.removeGameTeam(gameTeam);
        team.removeGameTeam(gameTeam);
    }

    private Game saveGame(League league, Member manager, GameRequest.Register request) {
        Game game = request.toEntity(manager, league);
        return gameRepository.save(game);
    }

    private void registerGameTeamAndLineup(Game game, GameRequest.TeamLineupRequest teamLineupInfo) {
        Team team = entityUtils.getEntity(teamLineupInfo.teamId(), Team.class);
        GameTeam gameTeam = GameTeam.of(game, team);
        game.addGameTeam(gameTeam);

        List<Long> teamPlayerIdsRequest = teamLineupInfo.lineupPlayers().stream()
                .map(GameRequest.LineupPlayerRequest::teamPlayerId)
                .toList();

        List<TeamPlayer> teamPlayers = teamPlayerRepository.findAllByTeamPlayerIds(teamPlayerIdsRequest);
        validateTeamPlayers(teamPlayerIdsRequest, teamPlayers, team);
        registerLineup(gameTeam, teamPlayers, teamLineupInfo);
    }

    private void registerLineup(GameTeam gameTeam, List<TeamPlayer> teamPlayers, GameRequest.TeamLineupRequest teamLineupRequest){
        Map<Long, GameRequest.LineupPlayerRequest> request = teamLineupRequest.lineupPlayers().stream()
                .collect(Collectors.toMap(
                        GameRequest.LineupPlayerRequest::teamPlayerId,
                        lineupPlayerRequest -> lineupPlayerRequest
                ));

        teamPlayers.forEach(teamPlayer -> {
            GameRequest.LineupPlayerRequest lineupPlayerRequest = request.get(teamPlayer.getId());

            if (lineupPlayerRequest == null) {
                throw new NotFoundException(PlayerErrorMessages.TEAM_PLAYER_NOT_FOUND_EXCEPTION);
            }

            LineupPlayer.of(
                    gameTeam,
                    teamPlayer.getPlayer(),
                    lineupPlayerRequest.state(),
                    teamPlayer.getJerseyNumber(),
                    lineupPlayerRequest.isCaptain()
            );
        });

    }

    private void validateTeamPlayers(List<Long> teamPlayerIdsRequest, List<TeamPlayer> teamPlayers, Team team) {
        if (new HashSet<>(teamPlayerIdsRequest).size() != teamPlayerIdsRequest.size()) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "요청에 중복된 선수 ID가 포함되어 있습니다.");
        }

        Set<Long> teamPlayerIds = teamPlayers.stream()
                .map(TeamPlayer::getId)
                .collect(Collectors.toSet());

        teamPlayerIdsRequest.stream()
                .filter(requestedId -> !teamPlayerIds.contains(requestedId))
                .findFirst()
                .ifPresent(invalidId -> {
                    throw new CustomException(HttpStatus.BAD_REQUEST,
                            PlayerErrorMessages.TEAM_PLAYER_NOT_FOUND_EXCEPTION + invalidId);
                });

        teamPlayers.forEach(tp -> {
            if (!tp.getTeam().equals(team)) {
                throw new CustomException(HttpStatus.BAD_REQUEST, GameErrorMessages.PLAYER_FROM_ANOTHER_TEAM_REGISTER_EXCEPTION);
            }
        });
    }
}