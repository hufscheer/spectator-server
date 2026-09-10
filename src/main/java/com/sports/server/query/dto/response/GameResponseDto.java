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
