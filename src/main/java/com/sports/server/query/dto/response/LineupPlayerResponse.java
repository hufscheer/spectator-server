package com.sports.server.query.dto.response;

import com.sports.server.command.game.domain.GameTeam;
import com.sports.server.command.game.domain.LineupPlayer;
import java.util.List;

public record LineupPlayerResponse(
        Long gameTeamId,
        String teamName,
        List<PlayerResponse> gameTeamPlayers

) {

    public LineupPlayerResponse(GameTeam gameTeam, List<LineupPlayer> lineupPlayers) {
        this(
                gameTeam.getId(),
                gameTeam.getLeagueTeam().getName(),
                lineupPlayers.stream()
                        .map(PlayerResponse::new)
                        .toList()
        );
    }

    public record PlayerResponse(
            String playerName,
            String description,
            int number,
            boolean isCaptain
    ) {
        public PlayerResponse(LineupPlayer player) {
            this(player.getName(), player.getDescription(), player.getNumber(), player.isCaptain());
        }
    }
}
