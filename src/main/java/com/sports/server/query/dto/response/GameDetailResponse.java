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
        QuarterResponse gameQuarter,
        String gameName,
        List<TeamResponse> gameTeams,
        String state,
        int round,
        boolean thirdPlaceMatch,
        boolean isPkTaken,
        Long leagueId,
        String leagueName
) {

    public GameDetailResponse(Game game, List<GameTeam> gameTeams) {
        this(
                game.getId(),
                game.getStartTime(),
                game.getVideoId(),
                QuarterResponse.from(game.getQuarter()),
                game.getName(),
                gameTeams.stream()
                        .sorted(Comparator.comparingLong(GameTeam::getId))
                        .map(TeamResponse::new)
                        .toList(),
                game.getState().name(),
                game.getRound().getNumber(),
                game.isThirdPlaceMatch(),
                game.getIsPkTaken(),
                game.getLeague().getId(),
                game.getLeague().getName()
        );
    }

    public record TeamResponse(
            Long gameTeamId,
            /**
             * 팀 원본 id. gameTeamId 는 경기마다 새로 생기는 값이라 리그 참가팀 목록
             * (GET /leagues/{id}/teams)과 이어 붙일 수 없었다. 매니저 경기 수정 화면이
             * 팀 이름 문자열로 두 응답을 짝지어 왔는데, 동명 팀이나 공백 차이에서 깨진다.
             */
            Long teamId,
            String gameTeamName,
            String logoImageUrl,
            Integer score,
            Integer pkScore,
            String teamColor
    ) {
        public TeamResponse(GameTeam gameTeam) {
            this(
                    gameTeam.getId(),
                    gameTeam.getTeam().getId(),
                    gameTeam.getTeam().getName(),
                    gameTeam.getTeam().getLogoImageUrl(),
                    gameTeam.getScore(),
                    gameTeam.getPkScore(),
                    gameTeam.getTeam().getTeamColor()
            );
        }
    }
}
