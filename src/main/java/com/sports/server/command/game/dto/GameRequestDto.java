package com.sports.server.command.game.dto;

import com.sports.server.command.game.domain.Game;
import com.sports.server.command.game.domain.GameState;
import com.sports.server.command.league.domain.League;
import com.sports.server.command.league.domain.Round;
import com.sports.server.command.member.domain.Member;
import com.sports.server.command.sport.domain.Sport;
import java.time.LocalDateTime;

public class GameRequestDto {
    public record Register(
            String name,
            String round,
            String quarter,
            String state,
            LocalDateTime startTime,
            Long idOfTeam1,
            Long idOfTeam2,
            String videoId
    ) {
        public Game toEntity(Sport sport, Member manager, League league) {
            return new Game(sport, manager, league, name, startTime, videoId, quarter, GameState.from(state),
                    Round.from(round));
        }
    }

    public record Update(
            String name,
            String round,
            String quarter,
            String state,
            LocalDateTime startTime,
            String videoId
    ) {
    }
}
