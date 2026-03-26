package com.sports.server.query.dto.response;

import com.sports.server.command.game.domain.Game;
import com.sports.server.command.game.domain.GameTeam;
import com.sports.server.command.league.domain.League;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

public record RecentLeagueGamesResponse(
        Long leagueId,
        String leagueName,
        String leagueProgress,
        List<GameResponse> games
) {
    public static RecentLeagueGamesResponse of(League league, String leagueProgress, List<GameResponse> games) {
        return new RecentLeagueGamesResponse(league.getId(), league.getName(), leagueProgress, games);
    }

    public record GameResponse(
            Long id,
            LocalDateTime startTime,
            String gameQuarter,
            String gameName,
            int round,
            String videoId,
            String gameState,
            List<TeamResponse> gameTeams,
            boolean isPkTaken
    ) {
        public GameResponse(final Game game, final List<GameTeam> gameTeams) {
            this(
                    game.getId(),
                    game.getStartTime(),
                    game.getGameQuarter(),
                    game.getName(),
                    game.getRound().getNumber(),
                    game.getVideoId(),
                    game.getState().name(),
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
}