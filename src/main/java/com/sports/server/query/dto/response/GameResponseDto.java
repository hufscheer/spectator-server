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
         * <p>이름은 {@code state} 다. 같은 값을 담는 응답이 넷인데 {@code GameDetailResponse}
         * (GET /games/{id}) 와 {@code LeagueResponseWithGames}(GET /leagues/{id}/games),
         * {@code LeagueResponseWithInProgressGames} 가 {@code state} 를 쓰고,
         * {@code RecentLeagueGamesResponse}(GET /leagues/recent/games) 하나만
         * {@code gameState} 다.
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
