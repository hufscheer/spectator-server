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

import com.sports.server.common.exception.BadRequestException;
import com.sports.server.common.exception.ExceptionMessages;
import com.sports.server.common.exception.NotFoundException;
import com.sports.server.common.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GameService {

    private final EntityUtils entityUtils;
    private final GameRepository gameRepository;
    private final TimelineRepository timelineRepository;
    private final TeamPlayerRepository teamPlayerRepository;
    private final LineupPlayerRepository lineupPlayerRepository;
    private final GameTeamRepository gameTeamRepository;
    private final LeagueTeamRepository leagueTeamRepository;

    @Transactional
    public Long register(final Long leagueId, final GameRequest.Register request, final Member administrator) {
        League league = entityUtils.getEntity(leagueId, League.class);
        PermissionValidator.checkPermission(league, administrator);
        league.validateRoundWithinLimit(request.round());

        validateGameTeamsInLeague(league, request.team1(), request.team2());

        Game game = saveGame(league, administrator, request);
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

    @Transactional(readOnly = true)
    public List<Game> findGamesByIds(List<Long> gameIds) {
        return gameRepository.findAllByIdIn(gameIds);
    }

    @Transactional
    public void updateGame(Long leagueId, Long gameId, GameRequest.Update request, Member administrator) {
        League league = entityUtils.getEntity(leagueId, League.class);
        PermissionValidator.checkPermission(league, administrator);
        league.validateRoundWithinLimit(request.round());

        Game game = entityUtils.getEntity(gameId, Game.class);
        GameState state = GameState.from(request.state());

        game.updateName(request.name());
        game.updateStartTime(request.startTime());
        game.updateVideoId(request.videoId());
        game.updateGameQuarter(request.quarter());
        game.updateState(state);
        game.updateRound(Round.from(request.round()));
        game.updateResult();
    }

    @Transactional
    public void deleteGame(Long leagueId, Long gameId, final Member administrator) {
        League league = entityUtils.getEntity(leagueId, League.class);
        PermissionValidator.checkPermission(league, administrator);

        Game game = entityUtils.getEntity(gameId, Game.class);
        timelineRepository.deleteByGame(game);
        gameRepository.delete(game);
    }

    @Transactional
    public void deleteGameTeam(final Long gameTeamId, final Member administrator) {
        GameTeam gameTeam = entityUtils.getEntity(gameTeamId, GameTeam.class);
        Game game = gameTeam.getGame();

        if (!game.isManagedBy(administrator)) {
            throw new UnauthorizedException(AuthorizationErrorMessages.PERMISSION_DENIED);
        }
        game.removeGameTeam(gameTeam);
    }

    private Game saveGame(League league, Member administrator, GameRequest.Register request) {
        Game game = request.toEntity(administrator, league);
        return gameRepository.save(game);
    }

    private void registerGameTeamAndLineup(Game game, GameRequest.TeamLineupRequest teamLineupInfo) {
        Team team = entityUtils.getEntity(teamLineupInfo.teamId(), Team.class);
        GameTeam gameTeam = GameTeam.of(game, team);
        gameTeamRepository.save(gameTeam);
        game.addGameTeam(gameTeam);
        team.addGameTeam(gameTeam);

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

        for (TeamPlayer teamPlayer : teamPlayers) {
            GameRequest.LineupPlayerRequest lineupPlayerRequest = request.get(teamPlayer.getId());

            if (lineupPlayerRequest == null) {
                throw new NotFoundException(PlayerErrorMessages.TEAM_PLAYER_NOT_FOUND_EXCEPTION);
            }

            LineupPlayer lineupPlayer = LineupPlayer.of(
                    gameTeam,
                    teamPlayer.getPlayer(),
                    lineupPlayerRequest.state(),
                    teamPlayer.getJerseyNumber(),
                    lineupPlayerRequest.isCaptain()
            );
            lineupPlayerRepository.save(lineupPlayer);
            gameTeam.addLineupPlayer(lineupPlayer);
        }
    }

    private void validateTeamPlayers(List<Long> teamPlayerIdsRequest, List<TeamPlayer> teamPlayers, Team team) {
        if (new HashSet<>(teamPlayerIdsRequest).size() != teamPlayerIdsRequest.size()) {
            throw new BadRequestException(ExceptionMessages.GAME_SERVICE_DUPLICATE_PLAYER_IDS);
        }

        Set<Long> teamPlayerIds = teamPlayers.stream()
                .map(TeamPlayer::getId)
                .collect(Collectors.toSet());

        teamPlayerIdsRequest.stream()
                .filter(requestedId -> !teamPlayerIds.contains(requestedId))
                .findFirst()
                .ifPresent(invalidId -> {
                    throw new NotFoundException(PlayerErrorMessages.TEAM_PLAYER_NOT_FOUND_EXCEPTION + invalidId);
                });

        teamPlayers.forEach(tp -> {
            if (!tp.getTeam().getId().equals(team.getId())) {
                throw new BadRequestException(GameErrorMessages.PLAYER_FROM_ANOTHER_TEAM_REGISTER_EXCEPTION);
            }
        });
    }

    private void validateGameTeamsInLeague(League league, GameRequest.TeamLineupRequest team1, GameRequest.TeamLineupRequest team2) {
        Set<Long> leagueTeamIds = new HashSet<>(leagueTeamRepository.findTeamIdsByLeagueId(league.getId()));

        if (!leagueTeamIds.contains(team1.teamId()) || !leagueTeamIds.contains(team2.teamId())) {
            throw new BadRequestException(GameErrorMessages.TEAM_NOT_IN_LEAGUE_TEAM);
        }
    }

}
