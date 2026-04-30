package com.sports.server.command.game.domain;

import static com.sports.server.support.fixture.FixtureMonkeyUtils.entityBuilder;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import com.sports.server.common.exception.CustomException;
import java.util.ArrayList;
import org.junit.jupiter.api.BeforeEach;
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

    @Test
    void 새로운_주장을_등록하면_기존_주장은_자동으로_해제된다() {
        // given
        LineupPlayer firstLineupPlayer = gameTeam.getLineupPlayers().get(0);
        LineupPlayer secondLineupPlayer = gameTeam.getLineupPlayers().get(1);
        gameTeam.changePlayerToCaptain(firstLineupPlayer);

        // when
        gameTeam.changePlayerToCaptain(secondLineupPlayer);

        // then
        assertThat(firstLineupPlayer.isCaptain()).isFalse();
        assertThat(secondLineupPlayer.isCaptain()).isTrue();
    }

    @Test
    void 이미_주장인_선수를_다시_주장으로_등록해도_예외가_발생하지_않는다() {
        // given
        LineupPlayer lineupPlayer = gameTeam.getLineupPlayers().get(0);
        gameTeam.changePlayerToCaptain(lineupPlayer);

        // when
        gameTeam.changePlayerToCaptain(lineupPlayer);

        // then
        assertThat(lineupPlayer.isCaptain()).isTrue();
    }
}

