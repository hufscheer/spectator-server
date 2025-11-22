package com.sports.server.command.game.dto;

import com.sports.server.command.game.domain.Game;
import com.sports.server.command.game.domain.GameState;
import com.sports.server.command.game.domain.LineupPlayerState;
import com.sports.server.command.league.domain.League;
import com.sports.server.command.league.domain.Round;
import com.sports.server.command.member.domain.Member;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class GameRequest {
    public record Register(
            String name,
            int round,
            String quarter,
            String state,
            LocalDateTime startTime,
            String videoId,
            TeamLineupRequest team1,
            TeamLineupRequest team2
    ) {
        public Game toEntity(Member administrator, League league) {
            return Game.builder()
                    .administrator(administrator)
                    .league(league)
                    .name(this.name())
                    .startTime(this.startTime())
                    .videoId(this.videoId())
                    .gameQuarter(this.quarter())
                    .state(GameState.from(this.state()))
                    .round(Round.from(this.round()))
                    .isPkTaken(false)
                    .build();
        }
    }

    public record TeamLineupRequest(
            Long teamId,
            List<LineupPlayerRequest> lineupPlayers
    ) {
    }

    public record LineupPlayerRequest(
            Long teamPlayerId,
            LineupPlayerState state,
            Boolean isCaptain
    ) {
        public LineupPlayerRequest {
            state = Optional.ofNullable(state).orElse(LineupPlayerState.STARTER);
            isCaptain = Optional.ofNullable(isCaptain).orElse(false);
        }
    }

    public record Update(
            String name,
            int round,
            String quarter,
            String state,
            LocalDateTime startTime,
            String videoId
    ) {
    }
}