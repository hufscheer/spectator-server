package com.sports.server.command.game.dto;

import com.sports.server.command.game.domain.Game;
import com.sports.server.command.game.domain.GameState;
import com.sports.server.command.game.domain.LineupPlayerState;
import com.sports.server.command.game.domain.Position;
import com.sports.server.command.league.domain.League;
import com.sports.server.command.league.domain.Round;
import com.sports.server.command.member.domain.Member;
import com.sports.server.command.league.domain.Quarter;
import com.sports.server.command.league.domain.QuarterResolver;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class GameRequest {

    /**
     * 3·4위전은 트리 밖 라운드라 숫자로 지목할 수 없어 별도 플래그로 받는다.
     */
    private interface RoundSelectable {
        int round();

        boolean thirdPlaceMatch();

        default Round resolveRound() {
            return thirdPlaceMatch() ? Round.THIRD_PLACE_MATCH : Round.from(round());
        }
    }

    public record Register(
            String name,
            int round,
            String quarter,
            String state,
            LocalDateTime startTime,
            String videoId,
            TeamLineupRequest team1,
            TeamLineupRequest team2,
            boolean thirdPlaceMatch
    ) implements RoundSelectable {
        public Game toEntity(Member administrator, League league) {
            return Game.builder()
                    .administrator(administrator)
                    .league(league)
                    .name(this.name())
                    .startTime(this.startTime())
                    .videoId(this.videoId())
                    .gameQuarter(QuarterResolver.resolve(this.quarter()).name())
                    .state(GameState.from(this.state()))
                    .round(this.resolveRound())
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
            Boolean isCaptain,
            Position position
    ) {
        public LineupPlayerRequest {
            state = Optional.ofNullable(state).orElse(LineupPlayerState.STARTER);
            isCaptain = Optional.ofNullable(isCaptain).orElse(false);
        }
    }

    public record Update(
            String name,
            int round,
            LocalDateTime startTime,
            String videoId,
            boolean thirdPlaceMatch
    ) implements RoundSelectable {
    }
}