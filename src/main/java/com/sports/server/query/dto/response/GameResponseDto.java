package com.sports.server.query.dto.response;

import com.sports.server.command.game.domain.Game;
import com.sports.server.command.game.domain.GameTeam;
import com.sports.server.command.sport.domain.Sport;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

public record GameResponseDto(
        Long id,
        LocalDateTime startTime,
        String gameQuarter,
        String gameName,
        Integer round,
        String videoId,
        List<TeamResponse> gameTeams,
        String sportsName
) {
    public GameResponseDto(final Game game, final List<GameTeam> gameTeams, final Sport sport) {
        this(
                game.getId(),
                game.getStartTime(),
                game.getGameQuarter(),
                game.getName(),
                game.getRound(),
                game.getVideoId(),
                gameTeams.stream()
                        .sorted(Comparator.comparingLong(GameTeam::getId))
                        .map(TeamResponse::new)
                        .toList(),
                sport.getName()
        );
    }

    public record TeamResponse(
            Long gameTeamId,
            String gameTeamName,
            String logoImageUrl,
            Integer score
    ) {
        public TeamResponse(GameTeam gameTeam) {
            this(
                    gameTeam.getId(),
                    gameTeam.getLeagueTeam().getName(),
                    gameTeam.getLeagueTeam().getLogoImageUrl(),
                    gameTeam.getScore()
            );
        }
    }
}
