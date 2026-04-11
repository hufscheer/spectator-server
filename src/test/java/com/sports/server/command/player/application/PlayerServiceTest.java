package com.sports.server.command.player.application;

import com.sports.server.command.organization.domain.Organization;
import com.sports.server.command.player.domain.Player;
import com.sports.server.command.player.domain.PlayerRepository;
import com.sports.server.command.player.dto.PlayerRequest;
import com.sports.server.common.application.EntityUtils;
import com.sports.server.common.exception.CustomException;
import com.sports.server.common.exception.ExceptionMessages;
import com.sports.server.common.exception.NotFoundException;
import com.sports.server.support.ServiceTest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.DisplayName;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@Sql("/member-fixture.sql")
public class PlayerServiceTest extends ServiceTest {
    @Autowired
    private PlayerService playerService;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private EntityUtils entityUtils;

    @Nested
    @DisplayName("Organization별 학번 자릿수 검증")
    class StudentNumberDigitsValidation {

        @Test
        void 학번_9자리_organization에서_9자리_학번으로_등록_성공() {
            // given
            Organization org = entityUtils.getEntity(1L, Organization.class); // student_number_digits = 9
            PlayerRequest.Register request = new PlayerRequest.Register("손흥민", "202500001");

            // when
            Long playerId = playerService.register(request, org);

            // then
            assertThat(playerId).isNotNull();
        }

        @Test
        void 학번_9자리_organization에서_10자리_학번으로_등록_시_예외가_발생한다() {
            // given
            Organization org = entityUtils.getEntity(1L, Organization.class); // student_number_digits = 9
            PlayerRequest.Register request = new PlayerRequest.Register("손흥민", "2025000001");

            // when & then
            assertThatThrownBy(() -> playerService.register(request, org))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(String.format(ExceptionMessages.PLAYER_STUDENT_NUMBER_INVALID, 9));
        }

        @Test
        void 학번_10자리_organization에서_10자리_학번으로_등록_성공() {
            // given
            Organization org = entityUtils.getEntity(4L, Organization.class); // student_number_digits = 10
            PlayerRequest.Register request = new PlayerRequest.Register("손흥민", "2025000001");

            // when
            Long playerId = playerService.register(request, org);

            // then
            assertThat(playerId).isNotNull();
        }

        @Test
        void 학번_10자리_organization에서_9자리_학번으로_등록_시_예외가_발생한다() {
            // given
            Organization org = entityUtils.getEntity(4L, Organization.class); // student_number_digits = 10
            PlayerRequest.Register request = new PlayerRequest.Register("손흥민", "202500001");

            // when & then
            assertThatThrownBy(() -> playerService.register(request, org))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(String.format(ExceptionMessages.PLAYER_STUDENT_NUMBER_INVALID, 10));
        }
    }

    @Test
    void 선수_등록_시_학번이_중복되면_예외가_발생한다() {
        // given
        String duplicatedStudentNumber = "202500001";
        Organization organization = entityUtils.getEntity(1L, Organization.class);
        playerRepository.save(new Player("손흥민", duplicatedStudentNumber, organization.getStudentNumberDigits()));

        // when & then
        PlayerRequest.Register request = new PlayerRequest.Register("박지성", duplicatedStudentNumber);

        assertThatThrownBy(() -> playerService.register(request, organization))
                .isInstanceOf(CustomException.class)
                .hasMessage("이미 존재하는 학번입니다.");
    }

    @Test
    void 삭제한_이후에는_해당_객체를_찾을_수_없다() {
        // given
        Organization organization = entityUtils.getEntity(1L, Organization.class);
        Player player = new Player("손흥민", "202500001", organization.getStudentNumberDigits());
        playerRepository.save(player);

        // when
        playerService.delete(player.getId());

        // then
        Assertions.assertThatThrownBy(
                        () -> entityUtils.getEntity(player.getId(), Player.class))
                .isInstanceOf(NotFoundException.class);
    }
}
