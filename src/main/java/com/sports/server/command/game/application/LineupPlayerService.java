package com.sports.server.command.game.application;

import com.sports.server.command.game.domain.Game;
import com.sports.server.command.game.domain.GameTeam;
import com.sports.server.command.game.domain.LineupPlayer;
import com.sports.server.command.game.domain.LineupPlayerRepository;
import com.sports.server.command.game.dto.GameRequest;
import com.sports.server.command.game.exception.GameErrorMessages;
import com.sports.server.command.player.exception.PlayerErrorMessages;
import com.sports.server.command.team.domain.TeamPlayer;
import com.sports.server.command.team.domain.TeamPlayerRepository;
import com.sports.server.common.application.EntityUtils;
import com.sports.server.common.exception.CustomException;
import com.sports.server.common.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class LineupPlayerService {

    private final EntityUtils entityUtils;
    private final TeamPlayerRepository teamPlayerRepository;
    private final LineupPlayerRepository lineupPlayerRepository;

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

    public void changePlayerToCaptain(final Long gameId, final Long lineupPlayerId) {
        Game game = entityUtils.getEntity(gameId, Game.class);
        LineupPlayer lineupPlayer = entityUtils.getEntity(lineupPlayerId, LineupPlayer.class);
        game.changePlayerToCaptain(lineupPlayer);
    }

    public void revokeCaptainFromPlayer(final Long gameId, final Long lineupPlayerId) {
        Game game = entityUtils.getEntity(gameId, Game.class);
        LineupPlayer lineupPlayer = entityUtils.getEntity(lineupPlayerId, LineupPlayer.class);
        game.revokeCaptainFromPlayer(lineupPlayer);
    }

    public Long addPlayerToLineup(final Long gameTeamId, final GameRequest.LineupPlayerRequest request) {
        GameTeam gameTeam = entityUtils.getEntity(gameTeamId, GameTeam.class);
        TeamPlayer teamPlayer = teamPlayerRepository.findById(request.teamPlayerId())
                .orElseThrow(() -> new NotFoundException(PlayerErrorMessages.TEAM_PLAYER_NOT_FOUND_EXCEPTION));

        validatePlayerForLineup(gameTeam, teamPlayer);

        LineupPlayer lineupPlayer = LineupPlayer.of(
                gameTeam,
                teamPlayer.getPlayer(),
                request.state(),
                teamPlayer.getJerseyNumber(),
                request.isCaptain()
        );
        lineupPlayerRepository.save(lineupPlayer);
        gameTeam.addLineupPlayer(lineupPlayer);
        return lineupPlayer.getId();
    }

    public void removePlayerFromLineup(final Long gameTeamId, final Long lineupPlayerId) {
        LineupPlayer lineupPlayer = entityUtils.getEntity(lineupPlayerId, LineupPlayer.class);
        GameTeam gameTeam = entityUtils.getEntity(gameTeamId, GameTeam.class);

        if (!lineupPlayer.getGameTeam().getId().equals(gameTeamId)) {
            throw new CustomException(HttpStatus.BAD_REQUEST, GameErrorMessages.LINEUP_PLAYER_NOT_IN_GAME_TEAM_EXCEPTION);
        }
        gameTeam.removeLineupPlayer(lineupPlayer);
    }

    private void validatePlayerForLineup(final GameTeam gameTeam, final TeamPlayer teamPlayer) {
        if (!teamPlayer.getTeam().getId().equals(gameTeam.getTeam().getId())) {
            throw new CustomException(HttpStatus.BAD_REQUEST, GameErrorMessages.PLAYER_FROM_ANOTHER_TEAM_REGISTER_EXCEPTION);
        }

        if (lineupPlayerRepository.existsByGameTeamAndPlayer(gameTeam, teamPlayer.getPlayer())) {
            throw new CustomException(HttpStatus.BAD_REQUEST, GameErrorMessages.ALREADY_REGISTERED_IN_LINEUP);
        }
    }
}
