package com.sports.server.query.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.tuple;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.sports.server.query.dto.response.LeagueTeamDetailResponse;
import com.sports.server.query.dto.response.LeagueTeamDetailResponse.LeagueTeamPlayerResponse;
import com.sports.server.query.dto.response.LeagueTeamResponse;
import com.sports.server.support.ServiceTest;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

@Sql(scripts = "/league-fixture.sql")
public class LeagueQueryServiceTest extends ServiceTest {

    @Autowired
    LeagueQueryService leagueQueryService;

    @Test
    void 리그에_해당하는_리그팀을_모두_조회한다() {
        // given
        Long leagueId = 1L;

        // when
        List<LeagueTeamResponse> leagueTeams = leagueQueryService.findTeamsByLeagueRound(leagueId, null);
        LeagueTeamResponse leagueTeam = leagueTeams.stream().filter(team -> team.leagueTeamId().equals(3L)).findFirst()
                .orElse(null);

        // then
        assertAll(
                () -> assertThat(leagueTeam.teamName()).isEqualTo("미컴 축구생각"),
                () -> assertThat(leagueTeam.sizeOfLeagueTeamPlayers()).isEqualTo(4)
        );

    }

    @Nested
    @DisplayName("리그팀의 상세 정보를 조회할 때")
    class LeagueTeamDetailedQueryTest {
        @Test
        void 정상적으로_조회된다() {
            // given
            Long leagueTeamId = 3L;

            // when
            LeagueTeamDetailResponse leagueTeam = leagueQueryService.findLeagueTeam(leagueTeamId);

            // then
            assertAll(
                    () -> assertThat(leagueTeam.teamName()).isEqualTo("미컴 축구생각"),
                    () -> assertThat(leagueTeam.logoImageUrl()).isEqualTo("이미지이미지"),
                    () -> assertThat(leagueTeam.leagueTeamPlayers()).hasSize(4),
                    () -> {
                        assertThat(leagueTeam.leagueTeamPlayers())
                                .extracting("id", "name", "number")
                                .containsExactlyInAnyOrder(
                                        tuple(1L, "봄동나물진승희", 0),
                                        tuple(2L, "가을전어이동규", 2),
                                        tuple(3L, "겨울붕어빵이현제", 3),
                                        tuple(4L, "여름수박고병룡", 3)
                                );
                    }
            );
        }

        @Test
        void 리그팀_선수가_ㄱㄴㄷ순으로_반환된다() {
            // given
            Long leagueTeamId = 1L;

            // when
            LeagueTeamDetailResponse leagueTeam = leagueQueryService.findLeagueTeam(leagueTeamId);

            // then
            assertAll(
                    () -> {
                        List<String> playerNames = leagueTeam.leagueTeamPlayers()
                                .stream()
                                .map(LeagueTeamPlayerResponse::name)
                                .toList();
                        assertThat(playerNames).isSortedAccordingTo(String.CASE_INSENSITIVE_ORDER);
                    }
            );
        }
    }


}
