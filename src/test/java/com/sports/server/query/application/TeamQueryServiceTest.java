package com.sports.server.query.application;

import com.sports.server.command.team.exception.TeamErrorMessages;
import com.sports.server.common.exception.NotFoundException;
import com.sports.server.query.dto.response.PlayerResponse;
import com.sports.server.query.dto.response.TeamDetailResponse;
import com.sports.server.query.dto.response.TeamResponse;
import com.sports.server.support.ServiceTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

@Sql(scripts = "/timeline-fixture.sql")
public class TeamQueryServiceTest extends ServiceTest {

    @Autowired
    private TeamQueryService teamQueryService;

    @Nested
    @DisplayName("단위별 팀 목록 조회 시")
    class GetAllTeamsByUnitsTest {

        @Test
        void 필터링할_단위가_없으면_모든_팀을_조회한다() {
            // when
            List<TeamResponse> responses = teamQueryService.getAllTeamsByUnits(null);

            // then
            assertThat(responses).hasSize(4);
        }

        @Test
        void 하나의_단위로_필터링하여_조회한다() {
            // given
            List<String> units = List.of("SOCIAL_SCIENCES");

            // when
            List<TeamResponse> responses = teamQueryService.getAllTeamsByUnits(units);

            // then
            assertAll(
                    () -> assertThat(responses).hasSize(2),
                    () -> assertThat(responses.get(0).name()).isEqualTo("팀A"),
                    () -> assertThat(responses.get(1).name()).isEqualTo("팀D")
            );
        }

        @Test
        void 여러_단위로_필터링하여_조회한다() {
            // given
            List<String> units = List.of("SOCIAL_SCIENCES", "ETC");

            // when
            List<TeamResponse> responses = teamQueryService.getAllTeamsByUnits(units);

            // then
            assertAll(
                    () -> assertThat(responses).hasSize(3)
            );
        }

        @Test
        void 존재하지_않는_단위로_필터링하면_예외가_발생한다() {
            // given
            List<String> units = List.of("INVALID_UNIT");

            // when & then
            assertThatThrownBy(() -> teamQueryService.getAllTeamsByUnits(units))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage(TeamErrorMessages.UNIT_NOT_FOUND_EXCEPTION);
        }
    }

    @Nested
    @DisplayName("팀 소속 선수 목록 조회 시")
    class GetAllTeamPlayersTest {

        @Test
        void 성공적으로_팀의_모든_선수를_조회한다() {
            // given
            Long teamId = 1L; // 팀A

            // when
            List<PlayerResponse> responses = teamQueryService.getAllTeamPlayers(teamId);

            // then
            assertAll(
                    () -> assertThat(responses).hasSize(5),
                    () -> assertThat(responses).extracting(PlayerResponse::name)
                            .containsExactlyInAnyOrder("선수1", "선수2", "선수3", "선수4", "선수5"),
                    () -> assertThat(responses).extracting(PlayerResponse::totalGoalCount)
                            .containsExactlyInAnyOrder(0, 1, 0, 0, 0)
            );
        }
    }

    @Nested
    @DisplayName("팀 상세 정보 조회 시")
    class GetTeamDetailTest {

        @Test
        void 성공적으로_팀의_상세_정보를_조회한다() {
            // given
            Long teamId = 1L; // 팀A

            // when
            TeamDetailResponse response = teamQueryService.getTeamDetail(teamId);

            // then
            assertAll(
                    () -> assertThat(response.name()).isEqualTo("팀A"),
                    () -> assertThat(response.winCount()).isEqualTo(1),
                    () -> assertThat(response.drawCount()).isZero(),
                    () -> assertThat(response.loseCount()).isZero(),
                    () -> assertThat(response.topScorers()).hasSize(1),
                    () -> assertThat(response.topScorers().get(0).playerName()).isEqualTo("선수2"),
                    () -> assertThat(response.topScorers().get(0).totalGoals()).isEqualTo(1),
                    () -> assertThat(response.topScorers().get(0).admissionYear()).isEqualTo("21"),
                    () -> assertThat(response.trophies().get(0).leagueId()).isEqualTo(1L),
                    () -> assertThat(response.trophies().get(0).leagueName()).isEqualTo("테스트 리그"),
                    () -> assertThat(response.trophies().get(0).trophyType()).isEqualTo("우승")
            );
        }
    }
}
