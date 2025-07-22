package com.sports.server.command.game.dto;

import com.sports.server.command.game.domain.Game;
import com.sports.server.command.game.domain.GameState;
import com.sports.server.command.league.domain.League;
import com.sports.server.command.league.domain.Round;
import com.sports.server.command.member.domain.Member;

import java.time.LocalDateTime;

public class GameRequestDto {
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
        public Game toEntity(Member manager, League league) {
            return new Game(manager, league, name, startTime, videoId, quarter, GameState.from(state),
                    Round.from(round), false);
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