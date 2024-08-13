package com.sports.server.query.dto.response;

import com.sports.server.command.game.domain.Game;
import com.sports.server.command.game.domain.GameState;
import com.sports.server.command.game.domain.GameTeam;
import com.sports.server.command.league.domain.League;
import java.time.LocalDateTime;
import java.util.List;

public record LeagueResponseWithGames(
        Long id,
        String name,
        int sizeOfLeagueTeams,
        String maxRound,
        LocalDateTime startAt,
        LocalDateTime endAt,
        List<GameDetailResponse> playingGames,
        List<GameDetailResponse> scheduledGames,
        List<GameDetailResponse> finishedGames

) {
    public static LeagueResponseWithGames of(League league, List<Game> games) {
        List<GameDetailResponse> playingGames = games.stream()
                .filter(g -> g.getState().equals(GameState.PLAYING)).map(GameDetailResponse::of).toList();

        List<GameDetailResponse> scheduledGames = games.stream()
                .filter(g -> g.getState().equals(GameState.SCHEDULED)).map(GameDetailResponse::of).toList();

        List<GameDetailResponse> finishedGames = games.stream()
                .filter(g -> g.getState().equals(GameState.FINISHED)).map(GameDetailResponse::of).toList();

        return new LeagueResponseWithGames(
                league.getId(), league.getName(), league.getLeagueTeams().size(),
                league.getMaxRound().getDescription(), league.getStartAt(), league.getEndAt(), playingGames,
                scheduledGames, finishedGames
        );
    }

    public record GameDetailResponse(
            Long id,
            String state,
            LocalDateTime startTime,
            List<GameTeamResponse> gameTeams
    ) {
        public record GameTeamResponse(
                Long gameTeamId,
                String gameTeamName,
                String logoImageUrl,
                Integer score
        ) {
            public static GameTeamResponse of(GameTeam gameTeam) {
                return new GameTeamResponse(
                        gameTeam.getId(),
                        gameTeam.getLeagueTeam().getName(),
                        gameTeam.getLeagueTeam().getLogoImageUrl(),
                        gameTeam.getScore()
                );
            }
        }

        public static GameDetailResponse of(final Game game) {
            return new GameDetailResponse(
                    game.getId(), game.getState().name(), game.getStartTime(),
                    game.getTeams().stream()
                            .map(GameTeamResponse::of)
                            .toList()
            );
        }

    }
}

