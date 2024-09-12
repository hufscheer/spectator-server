package com.sports.server.command.game.domain;

import static com.sports.server.support.fixture.FixtureMonkeyUtils.entityBuilder;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import com.sports.server.common.exception.CustomException;
import java.util.ArrayList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class GameTeamTest {
    private GameTeam gameTeam;

    @BeforeEach
    public void setUp() {
        gameTeam = entityBuilder(GameTeam.class)
                .set("lineupPlayers", new ArrayList<>())
                .sample();

        for (int i = 0; i < 2; i++) {
            gameTeam.getLineupPlayers().add(
                    entityBuilder(LineupPlayer.class)
                            .set("isCaptain", false)
                            .sample()
            );
        }
    }

    @Test
    void 해당_팀에_속하지_않는_선수인_경우_예외를_던진다() {
        // given
        LineupPlayer invalidPlayer = entityBuilder(LineupPlayer.class)
                .sample();

        // when & then
        assertThatThrownBy(() -> gameTeam.revokeCaptainFromPlayer(invalidPlayer))
                .isInstanceOf(CustomException.class)
                .hasMessage("해당 게임팀에 속하지 않는 선수입니다.");
    }

    @Nested
    @DisplayName("주장을 변경할 때")
    class ChangePlayerToCaptainTest {
        @Test
        void 이미_주장이_존재하는_경우_예외를_던진다() {
            // given
            LineupPlayer firstLineupPlayer = gameTeam.getLineupPlayers().get(0);
            LineupPlayer secondLineupPlayer = gameTeam.getLineupPlayers().get(1);
            gameTeam.changePlayerToCaptain(firstLineupPlayer);

            // when & then
            assertThatThrownBy(() -> gameTeam.changePlayerToCaptain(secondLineupPlayer))
                    .isInstanceOf(CustomException.class)
                    .hasMessage("이미 등록된 주장이 존재합니다.");
        }
    }
}

