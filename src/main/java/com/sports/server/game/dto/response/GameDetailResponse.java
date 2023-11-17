package com.sports.server.game.dto.response;

import com.sports.server.game.domain.Game;
import com.sports.server.game.domain.GameTeam;

import java.time.LocalDateTime;
import java.util.List;

public record GameDetailResponse(
        LocalDateTime startTime,
        String videoId,
        String gameQuarter,
        String gameName,
        List<TeamResponse> gameTeams
) {

    public GameDetailResponse(Game game, List<GameTeam> gameTeams) {
        this(
                game.getStartTime(),
                game.getVideoId(),
                game.getGameQuarter(),
                game.getName(),
                gameTeams.stream()
                        .map(TeamResponse::new)
                        .toList()
        );
    }

    public record TeamResponse(
            Long gameTeamId,
            String gameTeamName,
            String logoImageUrl,
            Integer score
    ) {
        public TeamResponse(GameTeam gameTeam) {
            this(
                    gameTeam.getId(),
                    gameTeam.getTeam().getName(),
                    gameTeam.getTeam().getLogoImageUrl(),
                    gameTeam.getScore()
            );
        }
    }
}
