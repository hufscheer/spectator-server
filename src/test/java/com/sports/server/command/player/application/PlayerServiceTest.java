package com.sports.server.command.player.application;

import com.sports.server.command.player.domain.Player;
import com.sports.server.command.player.domain.PlayerRepository;
import com.sports.server.command.player.dto.PlayerRequest;
import com.sports.server.common.application.EntityUtils;
import com.sports.server.common.exception.CustomException;
import com.sports.server.common.exception.NotFoundException;
import com.sports.server.support.ServiceTest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@ActiveProfiles("dev")
@Sql("/member-fixture.sql")
public class PlayerServiceTest extends ServiceTest {
    @Autowired
    private PlayerService playerService;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private EntityUtils entityUtils;

    @Test
    void 선수_등록_시_학번이_중복되면_예외가_발생한다() {
        // given
        String duplicatedStudentNumber = "202500001";
        playerRepository.save(new Player("손흥민", duplicatedStudentNumber));

        // when & then
        PlayerRequest.Register request = new PlayerRequest.Register("박지성", duplicatedStudentNumber);

        assertThatThrownBy(() -> playerService.register(request))
                .isInstanceOf(CustomException.class)
                .hasMessage("이미 존재하는 학번입니다.");
    }

    @Test
    void 삭제한_이후에는_해당_객체를_찾을_수_없다() {
        // given
        Player player = new Player("손흥민", "202500001");
        playerRepository.save(player);

        // when
        playerService.delete(player.getId());

        // then
        Assertions.assertThatThrownBy(
                        () -> entityUtils.getEntity(player.getId(), Player.class))
                .isInstanceOf(NotFoundException.class);
    }
}
