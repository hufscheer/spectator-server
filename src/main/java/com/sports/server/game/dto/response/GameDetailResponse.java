package com.sports.server.game.dto.response;

import com.sports.server.game.domain.Game;
import com.sports.server.game.domain.GameTeam;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
                IntStream.range(0, gameTeams.size())
                        .boxed()
                        .sorted(Comparator.comparingLong(i -> gameTeams.get(i).getId()))
                        .map(i -> new TeamResponse(gameTeams.get(i), i + 1))
                        .collect(Collectors.toList())
        );
    }

    public record TeamResponse(
            Long gameTeamId,
            String gameTeamName,
            String logoImageUrl,
            Integer score,
            int order
    ) {
        public TeamResponse(GameTeam gameTeam, int order) {
            this(
                    gameTeam.getId(),
                    gameTeam.getTeam().getName(),
                    gameTeam.getTeam().getLogoImageUrl(),
                    gameTeam.getScore(),
                    order
            );
        }
    }
}
