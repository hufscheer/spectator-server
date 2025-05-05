package com.sports.server.command.leagueteam.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class LeagueTeamPlayerTest {

    private LeagueTeam leagueTeam;
    private LeagueTeamPlayer leagueTeamPlayer;

    @BeforeEach
    public void setUp() {
        leagueTeam = new LeagueTeam();
        leagueTeamPlayer = new LeagueTeamPlayer(leagueTeam, "양효빈", 1, "202100000");
    }

    @Test
    void 학번이_9자리_숫자가_아니면_예외를_던진다() {
        // given
        String invalidValue = "2020033";

        // when & then
        assertThatThrownBy(() -> new LeagueTeamPlayer(leagueTeam, "진승희", 3, invalidValue))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Nested
    @DisplayName("LeagueTeamPlayer에서")
    class LeagueTeamPlayerUpdateTest {
        @Test
        void 리그팀_선수정보를_수정한다(){
            // when
            leagueTeamPlayer.update("진승희", 2, "202100001");

            //then
            assertAll(
                    () -> assertThat(leagueTeamPlayer.getName()).isEqualTo("진승희"),
                    () -> assertThat(leagueTeamPlayer.getNumber()).isEqualTo(2),
                    () -> assertThat(leagueTeamPlayer.getStudentNumber()).isEqualTo("202100001")
            );
        }

        @Test
        void 학번을_null값으로_수정한다(){
            // when
            leagueTeamPlayer.update("양효빈", 1, null);

            //then
            Assertions.assertThat(leagueTeamPlayer.getStudentNumber()).isEqualTo(null);
        }
    }
}
