package com.sports.server.command.game.application;

import com.sports.server.command.game.domain.*;
import com.sports.server.command.game.dto.GameRequest;
import com.sports.server.command.league.domain.*;
import com.sports.server.command.member.domain.Member;
import com.sports.server.command.team.domain.Team;
import com.sports.server.command.team.domain.TeamPlayer;
import com.sports.server.command.team.domain.TeamPlayerRepository;
import com.sports.server.command.timeline.domain.TimelineRepository;
import com.sports.server.common.application.EntityUtils;
import com.sports.server.common.application.PermissionValidator;
import java.time.LocalDateTime;
import java.util.List;

import com.sports.server.common.exception.CustomException;
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
    public void updateGameStatusToFinish(LocalDateTime now) {
        LocalDateTime cutoffTime = now.minusHours(5);
        List<Game> games = gameRepository.findGamesOlderThanFiveHours(cutoffTime);
        games.forEach(game -> game.updateState(GameState.FINISHED));
    }

    private Game saveGame(League league, Member manager, GameRequest.Register request) {
        Game game = request.toEntity(manager, league);
        return gameRepository.save(game);
    }

    private void registerGameTeamAndLineup(Game game, GameRequest.TeamLineupInfo teamLineupInfo) {
        Team team = entityUtils.getEntity(teamLineupInfo.teamId(), Team.class);
        GameTeam gameTeam = GameTeam.of(game, team);
        game.addGameTeam(gameTeam);

        List<Long> teamPlayerIds = teamLineupInfo.players().stream()
                .map(GameRequest.PlayerLineupInfo::teamPlayerId)
                .toList();

        List<TeamPlayer> teamPlayers = teamPlayerRepository.findByTeamPlayerIds(teamPlayerIds);
        validateTeamPlayers(teamPlayerIds, teamPlayers, team);

        teamPlayers.forEach(teamPlayer -> {
            GameRequest.PlayerLineupInfo info = teamLineupInfo.players().stream()
                    .filter(p -> p.teamPlayerId().equals(teamPlayer.getId()))
                    .findFirst()
                    .orElseThrow(() -> new CustomException(HttpStatus.NO_CONTENT, "요청 정보를 찾을 수 없습니다."));

            LineupPlayer.of(
                    gameTeam,
                    teamPlayer.getPlayer(),
                    info.state(),
                    teamPlayer.getJerseyNumber(),
                    info.isCaptain()
            );
        });
    }

    private void validateTeamPlayers(List<Long> requestedIds, List<TeamPlayer> teamPlayers, Team team) {
        if (teamPlayers.size() != requestedIds.size()) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "존재하지 않는 팀 소속 선수 ID가 포함되어 있습니다.");
        }

        teamPlayers.forEach(tp -> {
            if (!tp.getTeam().equals(team)) {
                throw new CustomException(HttpStatus.BAD_REQUEST, "다른 팀의 선수를 라인업에 등록할 수 없습니다.");
            }
        });
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
            throw new UnauthorizedException("해당 경기의 팀을 삭제할 권한이 없습니다.");
        }

        game.removeGameTeam(gameTeam);
        team.removeGameTeam(gameTeam);
    }

}