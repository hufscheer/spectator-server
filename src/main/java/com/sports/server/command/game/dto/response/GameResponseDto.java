package com.sports.server.command.game.dto.response;

import com.sports.server.command.sport.domain.Sport;
import com.sports.server.command.game.domain.Game;
import com.sports.server.command.game.domain.GameTeam;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public record GameResponseDto(
        Long id,
        LocalDateTime startTime,
        String gameQuarter,
        String gameName,
        List<TeamResponse> gameTeams,
        String sportsName
) {
    public GameResponseDto(final Game game, final List<GameTeam> gameTeams, final Sport sport) {
        this(
                game.getId(),
                game.getStartTime(),
                game.getGameQuarter(),
                game.getName(),
                IntStream.range(0, gameTeams.size())
                        .boxed()
                        .sorted(Comparator.comparingLong(i -> gameTeams.get(i).getId()))
                        .map(i -> new GameResponseDto.TeamResponse(gameTeams.get(i), i + 1))
                        .collect(Collectors.toList()),
                sport.getName()
        );
    }

    public record TeamResponse(
            Long gameTeamId,
            String gameTeamName,
            String logoImageUrl,
            Integer score,
            int order
    ) {
        public TeamResponse(GameTeam gameTeam, int order) {
            this(
                    gameTeam.getId(),
                    gameTeam.getTeam().getName(),
                    gameTeam.getTeam().getLogoImageUrl(),
                    gameTeam.getScore(), order
            );
        }
    }
}
