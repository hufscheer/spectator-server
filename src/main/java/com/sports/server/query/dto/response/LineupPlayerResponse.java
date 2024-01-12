package com.sports.server.query.dto.response;

import com.sports.server.command.game.domain.GameTeam;
import com.sports.server.command.game.domain.LineupPlayer;

import java.util.List;

public record LineupPlayerResponse(
        Long gameTeamId,
        String teamName,
        List<PlayerResponse> gameTeamPlayers,
        int order

) {

    public LineupPlayerResponse(GameTeam gameTeam, List<LineupPlayer> lineupPlayers, int order) {
        this(
                gameTeam.getId(),
                gameTeam.getTeam().getName(),
                lineupPlayers.stream()
                        .map(PlayerResponse::new)
                        .toList(),
                order
        );
    }

    public record PlayerResponse(
            String playerName,
            String description
    ) {
        public PlayerResponse(LineupPlayer player) {
            this(player.getName(), player.getDescription());
        }
    }
}
