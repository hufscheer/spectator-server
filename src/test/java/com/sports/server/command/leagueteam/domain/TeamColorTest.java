package com.sports.server.command.leagueteam.domain;

import com.sports.server.common.exception.CustomException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.Test;

class TeamColorTest {
    @Test
    void 유효한_팀_색이_아닌_경우_예외를_던진다() {
        // given
        String invalidTeamColor = "invalid";

        // when & then
        assertThatThrownBy(() -> TeamColor.fromHexCode(invalidTeamColor))
                .isInstanceOf(CustomException.class);
    }
}
