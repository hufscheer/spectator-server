package com.sports.server.command.player.application;

import com.sports.server.command.member.domain.Member;
import com.sports.server.command.member.domain.MemberRepository;
import com.sports.server.command.player.domain.Player;
import com.sports.server.command.player.domain.PlayerRepository;
import com.sports.server.command.player.dto.PlayerRequest;
import com.sports.server.common.application.EntityUtils;
import com.sports.server.common.exception.CustomException;
import com.sports.server.common.exception.ExceptionMessages;
import com.sports.server.common.exception.NotFoundException;
import com.sports.server.support.ServiceTest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
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

    @Autowired
    private MemberRepository memberRepository;

    private Member manager;

    @BeforeEach
    void setUp() {
        manager = memberRepository.findMemberByEmailWithOrganization("john@example.com")
                .orElseThrow();
    }

    @Nested
    @DisplayName("Organization별 학번 자릿수 검증")
    class StudentNumberDigitsValidation {

        @Test
        void 학번_9자리_organization에서_9자리_학번으로_등록_성공() {
            // given — manager의 organization은 student_number_digits = 9
            PlayerRequest.Register request = new PlayerRequest.Register("손흥민", "202500001");

            // when
            Long playerId = playerService.register(manager, request);

            // then
            assertThat(playerId).isNotNull();
        }

        @Test
        void 학번_9자리_organization에서_10자리_학번으로_등록_시_예외가_발생한다() {
            // given
            PlayerRequest.Register request = new PlayerRequest.Register("손흥민", "2025000001");

            // when & then
            assertThatThrownBy(() -> playerService.register(manager, request))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(String.format(ExceptionMessages.PLAYER_STUDENT_NUMBER_INVALID, 9));
        }

        @Test
        void 학번_10자리_organization에서_10자리_학번으로_등록_성공() {
            // given — organization id=4는 student_number_digits = 10
            Member manager10 = memberRepository.findMemberByEmailWithOrganization("user4@example.com")
                    .orElseThrow();
            PlayerRequest.Register request = new PlayerRequest.Register("손흥민", "2025000001");

            // when
            Long playerId = playerService.register(manager10, request);

            // then
            assertThat(playerId).isNotNull();
        }

        @Test
        void 학번_10자리_organization에서_9자리_학번으로_등록_시_예외가_발생한다() {
            // given
            Member manager10 = memberRepository.findMemberByEmailWithOrganization("user4@example.com")
                    .orElseThrow();
            PlayerRequest.Register request = new PlayerRequest.Register("손흥민", "202500001");

            // when & then
            assertThatThrownBy(() -> playerService.register(manager10, request))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(String.format(ExceptionMessages.PLAYER_STUDENT_NUMBER_INVALID, 10));
        }
    }

    @Test
    void 선수_등록_시_학번이_중복되면_예외가_발생한다() {
        // given
        String duplicatedStudentNumber = "202500001";
        playerService.register(manager, new PlayerRequest.Register("손흥민", duplicatedStudentNumber));

        // when & then
        PlayerRequest.Register request = new PlayerRequest.Register("박지성", duplicatedStudentNumber);

        assertThatThrownBy(() -> playerService.register(manager, request))
                .isInstanceOf(CustomException.class)
                .hasMessage("이미 존재하는 학번입니다.");
    }

    @Test
    void 학번_중복_예외에는_기존_선수_정보가_포함된다() {
        // given
        String duplicatedStudentNumber = "202500001";
        Long existingId = playerService.register(manager,
                new PlayerRequest.Register("손흥민", duplicatedStudentNumber));

        // when & then
        PlayerRequest.Register request = new PlayerRequest.Register("박지성", duplicatedStudentNumber);

        assertThatThrownBy(() -> playerService.register(manager, request))
                .isInstanceOfSatisfying(
                        com.sports.server.command.player.exception.PlayerStudentNumberConflictException.class,
                        ex -> {
                            assertThat(ex.getStatus()).isEqualTo(org.springframework.http.HttpStatus.CONFLICT);
                            assertThat(ex.getExistingPlayer().playerId()).isEqualTo(existingId);
                            assertThat(ex.getExistingPlayer().name()).isEqualTo("손흥민");
                            assertThat(ex.getExistingPlayer().studentNumber()).isEqualTo(duplicatedStudentNumber);
                            assertThat(ex.getExistingPlayer().teams()).isEmpty();
                        });
    }

    @Test
    void 다른_organization에서는_같은_학번으로_등록할_수_있다() {
        // given — 학교 A(org=1, manager: john)에 학번 등록
        String sameStudentNumber = "202500001";
        playerService.register(manager, new PlayerRequest.Register("손흥민", sameStudentNumber));

        // when — 학교 B(org=2, manager: smith) 가 동일 학번으로 등록
        Member smithManager = memberRepository.findMemberByEmailWithOrganization("smith@example.com")
                .orElseThrow();
        Long otherOrgPlayerId = playerService.register(smithManager,
                new PlayerRequest.Register("박지성", sameStudentNumber));

        // then — 다른 organization 이므로 학번 충돌 없이 등록 성공 (cross-org leak 차단)
        assertThat(otherOrgPlayerId).isNotNull();
    }

    @Test
    void 삭제한_이후에는_해당_객체를_찾을_수_없다() {
        // given
        Long playerId = playerService.register(manager, new PlayerRequest.Register("손흥민", "202500001"));

        // when
        playerService.delete(manager, playerId);

        // then
        Assertions.assertThatThrownBy(
                        () -> entityUtils.getEntity(playerId, Player.class))
                .isInstanceOf(NotFoundException.class);
    }
}
