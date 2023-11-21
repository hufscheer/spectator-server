package com.sports.server.game.dto.response;

import com.sports.server.game.domain.Game;
import com.sports.server.game.domain.GameTeam;
import com.sports.server.sport.domain.Sport;
import java.time.LocalDateTime;
import java.util.List;

public record GameResponseDto(
        LocalDateTime startTime,
        String gameQuarter,
        String gameName,
        List<TeamResponse> gameTeams,
        String sportsName
) {
    public GameResponseDto(final Game game, final List<GameTeam> gameTeams, final Sport sport) {
        this(
                game.getStartTime(),
                game.getGameQuarter(),
                game.getName(),
                gameTeams.stream()
                        .map(TeamResponse::new)
                        .toList(),
                sport.getName()
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
