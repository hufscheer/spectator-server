package com.sports.server.command.game.application;

import com.sports.server.command.game.domain.Game;
import com.sports.server.command.game.domain.GameTeam;
import com.sports.server.command.game.domain.GameTeamRepository;
import com.sports.server.command.game.domain.LineupPlayer;
import com.sports.server.common.application.EntityUtils;
import com.sports.server.common.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class LineupPlayerService {
    private final EntityUtils entityUtils;
    private final GameTeamRepository gameTeamRepository;

    public void changePlayerStateToStarter(final Long gameId, final Long lineupPlayerId) {
        Game game = entityUtils.getEntity(gameId, Game.class);
        LineupPlayer lineupPlayer = entityUtils.getEntity(lineupPlayerId, LineupPlayer.class);
        game.registerStarter(lineupPlayer);
    }

    public void changePlayerStateToCandidate(final Long gameId, final Long lineupPlayerId) {
        Game game = entityUtils.getEntity(gameId, Game.class);
        LineupPlayer lineupPlayer = entityUtils.getEntity(lineupPlayerId, LineupPlayer.class);
        game.rollbackToCandidate(lineupPlayer);
    }

    public void appointCaptain(final Long gameId, final Long gameTeamId, final Long lineupPlayerId) {
        Game game = entityUtils.getEntity(gameId, Game.class);

        GameTeam gameTeam = gameTeamRepository.findGameTeamByIdWithLineupPlayers(gameTeamId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 팀입니다."));
        validateGameTeam(game, gameTeam);

        LineupPlayer lineupPlayer = findLineupPlayer(gameTeam, lineupPlayerId);

        game.appointCaptain(gameTeam, lineupPlayer);
    }

    private LineupPlayer findLineupPlayer(final GameTeam gameTeam, final Long lineupPlayerId) {
        return gameTeam.getLineupPlayers().stream()
                .filter(lup -> lup.getId().equals(lineupPlayerId)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("해당 선수는 해당 팀에 속하지 않습니다."));
    }

    private void validateGameTeam(final Game game, final GameTeam gameTeam) {
        game.getTeams().stream()
                .filter(gt -> gt.equals(gameTeam)).findAny()
                .orElseThrow(() -> new IllegalArgumentException("해당 팀은 해당 경기에 속하지 않습니다."));
    }
}