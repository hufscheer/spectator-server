package com.sports.server.query.dto.response;

import com.sports.server.command.game.domain.Game;
import com.sports.server.command.game.domain.GameTeam;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

public record GameResponseDto(
        Long id,
        LocalDateTime startTime,
        QuarterResponse gameQuarter,
        String gameName,
        int round,
        boolean thirdPlaceMatch,
        String videoId,
        /**
         * 경기 상태(SCHEDULED·PLAYING·FINISHED). 요청에는 {@code state} 가 있는데 응답에는
         * 없어서, 관객 화면의 경기 카드 상태 배지가 빈 값으로 나왔다.
         *
         * <p><strong>객체가 경기 자체면 {@code state}, 경기를 참조하면 {@code gameState} 다.</strong>
         * 여기와 {@code GameDetailResponse} · {@code LeagueResponseWithGames} ·
         * {@code LeagueResponseWithInProgressGames} · {@code RecentLeagueGamesResponse} 는
         * 객체가 곧 경기라 {@code state} 이고, {@code BracketResponse.MatchResponse} 는
         * 대진 칸이라 {@code gameId} · {@code gameState} · {@code gameStartTime} 로 묶는다.
         */
        String state,
        List<TeamResponse> gameTeams,
        boolean isPkTaken
) {
    public GameResponseDto(final Game game, final List<GameTeam> gameTeams) {
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
            /**
             * 팀 원본 id. gameTeamId 는 경기마다 새로 생기는 값이라 리그 참가팀 목록
             * (GET /leagues/{id}/teams)이나 대진표와 이어 붙일 수 없다. #728 이 경기 단건
             * 응답에만 넣어서, 목록을 쓰는 화면은 여전히 팀 이름 문자열로 짝지어야 했다.
             */
            Long teamId,
            String gameTeamName,
            String logoImageUrl,
            Integer score,
            Integer pkScore
    ) {
        public TeamResponse(GameTeam gameTeam) {
            this(
                    gameTeam.getId(),
                    gameTeam.getTeam().getId(),
                    gameTeam.getTeam().getName(),
                    gameTeam.getTeam().getLogoImageUrl(),
                    gameTeam.getScore(),
                    gameTeam.getPkScore()
            );
        }
    }
}
