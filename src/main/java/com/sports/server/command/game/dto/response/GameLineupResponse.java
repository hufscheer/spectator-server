package com.sports.server.command.game.dto.response;

import com.sports.server.command.game.domain.GameTeam;
import com.sports.server.command.game.domain.GameTeamPlayer;
import java.util.List;

public record GameLineupResponse(
        Long gameTeamId,
        String teamName,
        List<PlayerResponse> gameTeamPlayers,
        int order

) {

    public GameLineupResponse(GameTeam gameTeam, List<GameTeamPlayer> gameTeamPlayers, int order) {
        this(
                gameTeam.getId(),
                gameTeam.getTeam().getName(),
                gameTeamPlayers.stream()
                        .map(PlayerResponse::new)
                        .toList(),
                order
        );
    }

    public record PlayerResponse(
            String playerName,
            String description
    ) {
        public PlayerResponse(GameTeamPlayer player) {
            this(player.getName(), player.getDescription());
        }
    }
}
