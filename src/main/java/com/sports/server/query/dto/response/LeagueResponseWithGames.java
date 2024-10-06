package com.sports.server.query.dto.response;

import com.sports.server.command.game.domain.Game;
import com.sports.server.command.game.domain.GameState;
import com.sports.server.command.league.domain.League;
import java.time.LocalDateTime;
import java.util.List;

public record LeagueResponseWithGames(
        Long id,
        String name,
        int sizeOfLeagueTeams,
        int maxRound,
        LocalDateTime startAt,
        LocalDateTime endAt,
        List<GameDetail> playingGames,
        List<GameDetail> scheduledGames,
        List<GameDetail> finishedGames

) {
    public static LeagueResponseWithGames of(League league, List<Game> games) {
        List<GameDetail> playingGames = games.stream()
                .filter(g -> g.getState().equals(GameState.PLAYING)).map(GameDetail::of).toList();

        List<GameDetail> scheduledGames = games.stream()
                .filter(g -> g.getState().equals(GameState.SCHEDULED)).map(GameDetail::of).toList();

        List<GameDetail> finishedGames = games.stream()
                .filter(g -> g.getState().equals(GameState.FINISHED)).map(GameDetail::of).toList();

        return new LeagueResponseWithGames(
                league.getId(), league.getName(), league.getLeagueTeams().size(),
                league.getMaxRound().getNumber(), league.getStartAt(), league.getEndAt(), playingGames,
                scheduledGames, finishedGames
        );
    }

    public record GameDetail(
            Long id,
            String state,
            LocalDateTime startTime,
            boolean isPkTaken,
            List<GameTeam> gameTeams
    ) {
        public record GameTeam(
                Long gameTeamId,
                String gameTeamName,
                String logoImageUrl,
                Integer score,
                Integer pkScore
        ) {
            public static GameTeam of(com.sports.server.command.game.domain.GameTeam gameTeam) {
                return new GameTeam(
                        gameTeam.getId(),
                        gameTeam.getLeagueTeam().getName(),
                        gameTeam.getLeagueTeam().getLogoImageUrl(),
                        gameTeam.getScore(),
                        gameTeam.getPkScore()
                );
            }
        }

        public static GameDetail of(final Game game) {
            return new GameDetail(
                    game.getId(), game.getState().name(), game.getStartTime(), game.getIsPkTaken(),
                    game.getTeams().stream()
                            .map(GameTeam::of)
                            .toList()
            );
        }

    }
}

