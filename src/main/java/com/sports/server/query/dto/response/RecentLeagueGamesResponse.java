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
        String sportType,
        List<GameResponse> games
) {
    public static RecentLeagueGamesResponse of(League league, String leagueProgress, List<GameResponse> games) {
        return new RecentLeagueGamesResponse(league.getId(), league.getName(), leagueProgress, league.getSportType().name(), games);
    }

    public record GameResponse(
            Long id,
            LocalDateTime startTime,
            QuarterResponse gameQuarter,
            String gameName,
            int round,
            boolean thirdPlaceMatch,
            String videoId,
            /**
             * 이 객체는 경기 자체라서 접두사를 붙이지 않는다. 형제가 {@code id} ·
             * {@code startTime} · {@code videoId} 로 전부 접두사가 없는데 이 필드만
             * {@code gameState} 였고, 같은 값을 담는 다른 응답 넷도 전부 {@code state} 다.
             *
             * <p>대진표({@code BracketResponse.MatchResponse})의 {@code gameState} 는 그대로
             * 둔다. 거긴 객체가 경기가 아니라 대진 칸이고 {@code gameId} · {@code gameState} ·
             * {@code gameStartTime} 이 "이 칸에 붙은 경기" 한 묶음이라 접두사가 있어야 한다.
             */
            String state,
            List<TeamResponse> gameTeams,
            boolean isPkTaken
    ) {
        public GameResponse(final Game game, final List<GameTeam> gameTeams) {
            this(
                    game.getId(),
                    game.getStartTime(),
                    QuarterResponse.from(game.getQuarter()),
                    game.getName(),
                    game.getRound().getNumber(),
                    game.isThirdPlaceMatch(),
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