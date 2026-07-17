package com.sports.server.query.dto.response;

import com.sports.server.command.bracket.domain.Bracket;
import com.sports.server.command.bracket.domain.BracketMatch;
import com.sports.server.command.game.domain.Game;
import com.sports.server.command.team.domain.Team;
import java.time.LocalDateTime;
import java.util.List;

public record BracketResponse(
        int size,
        List<RoundResponse> rounds
) {
    public static BracketResponse of(final Bracket bracket) {
        List<RoundResponse> rounds = bracket.roundNumbers().stream()
                .map(roundNumber -> RoundResponse.of(bracket, roundNumber))
                .toList();
        return new BracketResponse(bracket.getSize(), rounds);
    }

    public record RoundResponse(
            int round,
            List<MatchResponse> matches
    ) {
        private static RoundResponse of(final Bracket bracket, final int roundNumber) {
            List<MatchResponse> matches = bracket.matchesOf(roundNumber).stream()
                    .map(match -> MatchResponse.of(bracket, match))
                    .toList();
            return new RoundResponse(roundNumber, matches);
        }
    }

    public record MatchResponse(
            Long id,
            int matchNumber,
            TeamResponse team1,
            TeamResponse team2,
            Long gameId,
            String gameState,
            LocalDateTime gameStartTime,
            Long winnerTeamId
    ) {
        private static MatchResponse of(final Bracket bracket, final BracketMatch match) {
            Team slot1 = bracket.slot1Of(match);
            Team slot2 = bracket.slot2Of(match);
            Team display1 = slot1;
            Team display2 = slot2;

            Game game = match.getGame();
            if (game != null && game.getGameTeams().size() == Game.MINIMUM_TEAMS) {
                Team gameTeam1 = game.getTeam1().getTeam();
                Team gameTeam2 = game.getTeam2().getTeam();
                boolean shouldSwap = (slot1 != null && slot1.equals(gameTeam2))
                        || (slot2 != null && slot2.equals(gameTeam1));
                display1 = shouldSwap ? gameTeam2 : gameTeam1;
                display2 = shouldSwap ? gameTeam1 : gameTeam2;
            }

            Team advancer = bracket.advancerOf(match);
            return new MatchResponse(
                    match.getId(),
                    match.getMatchNumber(),
                    TeamResponse.of(display1),
                    TeamResponse.of(display2),
                    game == null ? null : game.getId(),
                    game == null ? null : game.getState().name(),
                    game == null ? null : game.getStartTime(),
                    advancer == null ? null : advancer.getId()
            );
        }
    }

    public record TeamResponse(
            Long teamId,
            String name,
            String logoImageUrl
    ) {
        private static TeamResponse of(final Team team) {
            if (team == null) {
                return null;
            }
            return new TeamResponse(team.getId(), team.getName(), team.getLogoImageUrl());
        }
    }
}
