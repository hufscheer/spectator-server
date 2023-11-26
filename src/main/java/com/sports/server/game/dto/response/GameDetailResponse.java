package com.sports.server.game.dto.response;

import static java.util.Comparator.comparingLong;

import com.sports.server.game.domain.Game;
import com.sports.server.game.domain.GameTeam;
import java.time.LocalDateTime;
import java.util.List;

public record GameDetailResponse(
        LocalDateTime startTime,
        String videoId,
        String gameQuarter,
        String gameName,
        String sportName,
        List<TeamResponse> gameTeams
) {

    public GameDetailResponse(Game game, List<GameTeam> gameTeams) {
        this(
                game.getStartTime(),
                game.getVideoId(),
                game.getGameQuarter(),
                game.getName(),
                game.getSport().getName(),
                gameTeams.stream()
                        .sorted(comparingLong(GameTeam::getId))
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
