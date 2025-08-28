package com.sports.server.query.dto.response;

import com.sports.server.command.game.domain.Game;
import com.sports.server.command.game.domain.GameTeam;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

public record GameDetailResponse(
        Long gameId,
        LocalDateTime startTime,
        String videoId,
        String gameQuarter,
        String gameName,
        List<TeamResponse> gameTeams,
        String state,
        int round,
        boolean isPkTaken,
        Long leagueId,
        String leagueName
) {

    public GameDetailResponse(Game game, List<GameTeam> gameTeams) {
        this(
                game.getId(),
                game.getStartTime(),
                game.getVideoId(),
                game.getGameQuarter(),
                game.getName(),
                gameTeams.stream()
                        .sorted(Comparator.comparingLong(GameTeam::getId))
                        .map(TeamResponse::new)
                        .toList(),
                game.getState().name(),
                game.getRound().getNumber(),
                game.getIsPkTaken(),
                game.getLeague().getId(),
                game.getLeague().getName()
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
