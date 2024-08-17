package com.sports.server.command.game.application;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import com.sports.server.command.game.domain.LineupPlayer;
import com.sports.server.common.exception.NotFoundException;
import com.sports.server.support.ServiceTest;
import com.sports.server.support.fixture.LineupPlayerFixtureRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

@Sql("/game-fixture.sql")
public class LineupPlayerServiceTest extends ServiceTest {

    @Autowired
    private LineupPlayerService lineupPlayerService;

    @Autowired
    private LineupPlayerFixtureRepository lineupPlayerFixtureRepository;

    @Nested
    @DisplayName("주장과 관련한 상태를 변경할 때")
    class appointCaptainTest {

        @Test
        void 게임에_속하지_않는_게임팀에_대해_요청할_경우_예외를_던진다() {
            // given
            Long gameId = 1L;
            Long invalidGameTeamId = 3L;
            Long lineupPlayerId = 1L;

            // when & then
            assertThatThrownBy(() -> lineupPlayerService.appointCaptain(gameId, invalidGameTeamId, lineupPlayerId))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("해당 팀은 해당 경기에 속하지 않습니다.");
        }

        @Test
        void 게임팀에_속하지_않는_라인업_선수에_대해_요청할_경우_예외를_던진다() {
            // given
            Long gameId = 1L;
            Long gameTeamId = 1L;
            Long invalidLineupPlayerId = 6L;

            // when & then
            assertThatThrownBy(() -> lineupPlayerService.appointCaptain(gameId, gameTeamId, invalidLineupPlayerId))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("해당 선수는 해당 팀에 속하지 않습니다.");
        }

        @Test
        void 정상적으로_변경된다() {
            // given
            Long gameId = 1L;
            Long gameTeamId = 1L;
            Long lineupPlayerId = 1L;

            // when
            lineupPlayerService.appointCaptain(gameId, gameTeamId, lineupPlayerId);

            // then
            LineupPlayer lineupPlayer = lineupPlayerFixtureRepository.findById(lineupPlayerId)
                    .orElseThrow(() -> new NotFoundException("존재하지 않는 라인업 선수입니다."));
            assertThat(lineupPlayer.isCaptain()).isEqualTo(true);
        }

        @Test
        void 두명_이상을_등록하지_못한다() {
            // given
            Long gameId = 2L;
            Long gameTeamId = 3L;
            Long lineupPlayerId = 12L;

            // when & then
            assertThatThrownBy(() -> lineupPlayerService.appointCaptain(gameId, gameTeamId, lineupPlayerId))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("주장은 두 명 이상 등록할 수 없습니다.");
        }

        @Test
        void 이미_주장인_경우에는_주장을_해제한다() {
            // given
            Long gameId = 2L;
            Long gameTeamId = 3L;
            Long lineupPlayerId = 11L;

            // when
            lineupPlayerService.appointCaptain(gameId, gameTeamId, lineupPlayerId);

            // then
            LineupPlayer lineupPlayer = lineupPlayerFixtureRepository.findById(lineupPlayerId)
                    .orElseThrow(() -> new NotFoundException("존재하지 않는 라인업 선수입니다."));
            assertThat(lineupPlayer.isCaptain()).isEqualTo(false);

        }

    }
}
