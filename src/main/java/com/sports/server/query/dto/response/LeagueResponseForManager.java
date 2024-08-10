package com.sports.server.query.dto.response;

import com.sports.server.command.game.domain.Game;
import com.sports.server.command.game.domain.GameTeam;
import com.sports.server.command.league.domain.League;
import java.time.LocalDateTime;
import java.util.List;

public record LeagueResponseForManager(

        Long id,
        String name,
        String state,
        int sizeOfLeagueTeams,
        String maxRound,
        LocalDateTime startAt,
        LocalDateTime endAt,
        List<GameDetailResponse> inProgressGames

) {
    public static LeagueResponseForManager of(League league, String state, List<Game> games) {
        return new LeagueResponseForManager(
                league.getId(), league.getName(), state, league.getLeagueTeams().size(),
                league.getMaxRound().getDescription(), league.getStartAt(), league.getEndAt(),
                games.stream()
                        .map(GameDetailResponse::of)
                        .toList()
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

