package com.sports.server.command.player.application;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.sports.server.command.member.domain.Member;
import com.sports.server.command.member.domain.MemberRepository;
import com.sports.server.command.player.domain.Player;
import com.sports.server.command.player.exception.PlayerErrorMessages;
import com.sports.server.common.application.EntityUtils;
import com.sports.server.common.exception.BadRequestException;
import com.sports.server.common.exception.NotFoundException;
import com.sports.server.support.ServiceTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

@Sql("/game-fixture.sql")
@DisplayName("선수 삭제 시")
public class PlayerDeleteTest extends ServiceTest {

    private static final Long PLAYER_IN_LINEUP = 11L;
    private static final Long PLAYER_WITHOUT_LINEUP = 1L;

    @Autowired
    private PlayerService playerService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private EntityUtils entityUtils;

    private Member manager;

    @BeforeEach
    void setUp() {
        manager = memberRepository.findMemberByEmailWithOrganization("john.doe@example.com").orElseThrow();
    }

    @Test
    void 라인업에_올라간_선수는_지울_수_없다() {
        assertThatThrownBy(() -> playerService.delete(manager, PLAYER_IN_LINEUP))
                .isInstanceOf(BadRequestException.class)
                .hasMessage(PlayerErrorMessages.CANNOT_DELETE_WITH_LINEUPS);
    }

    @Test
    void 라인업에_없는_선수는_지울_수_있다() {
        playerService.delete(manager, PLAYER_WITHOUT_LINEUP);

        assertThatThrownBy(() -> entityUtils.getEntity(PLAYER_WITHOUT_LINEUP, Player.class))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void 막힌_삭제는_예외로_끝나고_FK_위반까지_가지_않는다() {
        assertThatThrownBy(() -> playerService.delete(manager, PLAYER_IN_LINEUP))
                .isInstanceOf(BadRequestException.class);

        // 선수는 그대로 남아 있어야 한다 — 실패한 삭제가 절반만 반영되면 안 된다
        assertThatCode(() -> entityUtils.getEntity(PLAYER_IN_LINEUP, Player.class))
                .doesNotThrowAnyException();
    }
}
