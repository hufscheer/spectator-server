package com.sports.server.command.team.domain;

import com.sports.server.command.player.domain.Player;
import com.sports.server.common.exception.CustomException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

public class TeamTest {

    @Nested
    @DisplayName("팀 선수 관리 시")
    class ManageTeamPlayer {
        private Team team;
        private Player player1;
        private Player player2;

        @BeforeEach
        void setUp() {
            team = Team.builder()
                    .name("팀1")
                    .unit(Unit.SOCIAL_SCIENCES)
                    .logoImageUrl("image-url")
                    .teamColor("color")
                    .build();

            player1 = new Player("손흥민", "202500001");
            player2 = new Player("이강인", "202500002");
        }

        @Test
        void 새로운_선수를_팀에_추가한다(){
            // when
            team.addPlayer(player1, 10);

            // then
            assertThat(team.getTeamPlayers()).hasSize(1);
            assertThat(team.getTeamPlayers().get(0).getPlayer()).isEqualTo(player1);

            assertThat(player1.getTeamPlayers()).hasSize(1);
            assertThat(player1.getTeamPlayers().get(0).getPlayer()).isEqualTo(player1);
        }

        @Test
        void 기존에_소속된_선수를_추가하면_예외가_발생한다(){
            // given
            team.addPlayer(player1, 10);

            // when & then
            assertThatThrownBy(() -> team.addPlayer(player1, 10))
                    .isInstanceOf(CustomException.class)
                    .hasMessage("이미 팀에 소속된 선수입니다.");
        }

        @Test
        void 팀에서_선수를_제거한다(){
            // given
            team.addPlayer(player1, 10);
            team.addPlayer(player2, 5);

            // when
            team.removeTeamPlayer(player1);

            // then
            assertThat(team.getTeamPlayers()).hasSize(1);
            assertThat(team.getTeamPlayers().get(0).getPlayer()).isEqualTo(player2);
            assertThat(player1.getTeamPlayers()).isEmpty();
        }
    }
}
