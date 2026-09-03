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

        Boolean thirdPlaceMatch();

        default Round resolveRound() {
            return Boolean.TRUE.equals(thirdPlaceMatch()) ? Round.THIRD_PLACE_MATCH : Round.from(round());
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
            Boolean thirdPlaceMatch
    ) implements RoundSelectable {
        public Register {
            thirdPlaceMatch = Optional.ofNullable(thirdPlaceMatch).orElse(false);
        }

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

    /**
     * {@code thirdPlaceMatch} 는 래퍼 타입이다. primitive 로 두면 클라이언트가 필드를 빼먹었을 때
     * Jackson 이 false 로 채워, 이름만 고쳐도 3·4위전 지정이 조용히 풀린다. null 은 "변경 없음" 이다.
     */
    public record Update(
            String name,
            int round,
            LocalDateTime startTime,
            String videoId,
            Boolean thirdPlaceMatch
    ) implements RoundSelectable {
    }
}