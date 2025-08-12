package com.sports.server.query.dto.response;

import com.sports.server.command.game.domain.Game;
import com.sports.server.command.game.domain.GameTeam;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

public record GameResponseDto(
        Long id,
        LocalDateTime startTime,
        String gameQuarter,
        String gameName,
        int round,
        String videoId,
        List<TeamResponse> gameTeams,
        boolean isPkTaken
) {
    public GameResponseDto(final Game game, final List<GameTeam> gameTeams) {
        this(
                game.getId(),
                game.getStartTime(),
                game.getGameQuarter(),
                game.getName(),
                game.getRound().getNumber(),
                game.getVideoId(),
                gameTeams.stream()
                        .sorted(Comparator.comparingLong(GameTeam::getId))
                        .map(TeamResponse::new)
                        .toList(),
                game.getIsPkTaken()
        );
    }

    public record TeamResponse(
            Long gameTeamId,
            String gameTeamName,
            String logoImageUrl,
            Integer score,
            Integer pkScore
    ) {
        public TeamResponse(GameTeam gameTeam) {
            this(
                    gameTeam.getId(),
                    gameTeam.getTeam().getName(),
                    gameTeam.getTeam().getLogoImageUrl(),
                    gameTeam.getScore(),
                    gameTeam.getPkScore()
            );
        }
    }
}
