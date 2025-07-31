package com.sports.server.command.game.dto;

import com.sports.server.command.game.domain.Game;
import com.sports.server.command.game.domain.GameState;
import com.sports.server.command.league.domain.League;
import com.sports.server.command.league.domain.Round;
import com.sports.server.command.member.domain.Member;

import java.time.LocalDateTime;

public class GameRequest {
    public record Register(
            String name,
            int round,
            String quarter,
            String state,
            LocalDateTime startTime,
            Long idOfTeam1,
            Long idOfTeam2,
            String videoId
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