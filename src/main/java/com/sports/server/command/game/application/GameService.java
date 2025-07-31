package com.sports.server.command.game.application;

import com.sports.server.command.game.domain.*;
import com.sports.server.command.game.dto.GameRequest;
import com.sports.server.command.league.domain.*;
import com.sports.server.command.member.domain.Member;
import com.sports.server.command.team.domain.Team;
import com.sports.server.command.timeline.domain.TimelineRepository;
import com.sports.server.common.application.EntityUtils;
import com.sports.server.common.application.PermissionValidator;
import java.time.LocalDateTime;
import java.util.List;

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
    private final LeagueTeamRepository leagueTeamRepository;

    @Transactional
    public Long register(final Long leagueId, final GameRequest.Register request, final Member manager) {
        League league = entityUtils.getEntity(leagueId, League.class);
        PermissionValidator.checkPermission(league, manager);
        league.validateRoundWithinLimit(request.round());

        Game game = saveGame(league, manager, request);
        saveGameTeamsAndLineupPlayers(game, request);

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

    private void saveGameTeamsAndLineupPlayers(Game game, GameRequest.Register request) {
        Team team1 = entityUtils.getEntity(request.idOfTeam1(), Team.class);
        Team team2 = entityUtils.getEntity(request.idOfTeam2(), Team.class);

        registerLineup(game, team1);
        registerLineup(game, team2);
    }

    private void registerLineup(Game game, Team team) {
        LeagueTeam leagueTeam = leagueTeamRepository.findByLeagueAndTeam(game.getLeague(), team)
                .orElseThrow(() -> new IllegalArgumentException("팀이 리그에 등록되지 않았습니다."));

        GameTeam gameTeam = GameTeam.of(game, team);
        game.addGameTeam(gameTeam);

        for (LeagueTeamPlayer leagueTeamPlayer : leagueTeam.getLeagueTeamPlayers()) {
            LineupPlayer.of(
                    gameTeam,
                    leagueTeamPlayer,
                    LineupPlayerState.CANDIDATE
            );
        }
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