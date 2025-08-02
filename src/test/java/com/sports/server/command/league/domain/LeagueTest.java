package com.sports.server.command.league.domain;

import static com.sports.server.support.fixture.FixtureMonkeyUtils.entityBuilder;
import static com.sports.server.support.fixture.FixtureMonkeyUtils.maxRoundArbitrary;
import static com.sports.server.support.fixture.FixtureMonkeyUtils.nameArbitrary;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Named.named;

import com.sports.server.command.league.dto.LeagueRequestDto;
import com.sports.server.command.member.domain.Member;
import com.sports.server.command.organization.domain.Organization;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;
import net.jqwik.api.Arbitraries;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class LeagueTest {
    private Member manager;
    private Organization organization;

    @BeforeEach
    void setUp() {
        manager = entityBuilder(Member.class).sample();
        organization = entityBuilder(Organization.class).sample();
    }

    @Nested
    @DisplayName("리그 생성 시")
    class CreateLeague {
        @Test
        void isDeleted가_false로_생성된다() {
            // given
            League sut;

            // when
            sut = new League(manager, organization, nameArbitrary().sample(), LocalDateTime.now(),
                    LocalDateTime.now(),
                    maxRoundArbitrary().sample());

            // then
            assertThat(sut.isDeleted()).isEqualTo(false);
        }

        @Test
        void 리그의_현재_라운드와_총_라운드는_같다() {
            // given
            League sut;

            // when
            sut = new League(manager, organization, nameArbitrary().sample(), LocalDateTime.now(), LocalDateTime.now(),
                    maxRoundArbitrary().sample());

            // then
            assertThat(sut.getMaxRound()).isEqualTo(sut.getInProgressRound());
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("leagueUpdateRequestStream")
    @DisplayName("리그 정보 수정 테스트")
    void 리그_정보_수정(LeagueRequestDto.Update request, List<Function<League, Executable>> assertions) throws Exception {
        // given
        League sut = new League(
                manager,
                organization,
                Arbitraries.strings()
                        .ofMinLength(5).sample(),
                LocalDateTime.of(20, 12, 11, 0, 0, 0),
                LocalDateTime.of(20, 12, 12, 0, 0, 0),
                Round.from(8)
        );

        // when
        sut.updateInfo(request.name(), request.startAt(), request.endAt(), Round.from(request.maxRound()));

        // then
        assertAll(
                assertions.stream().map(assertion -> assertion.apply(sut))
        );
    }

    private static Stream<Arguments> leagueUpdateRequestStream() {
        String emptyName = "";
        String newName = "레몬즙 입에 물고 참기 대회";
        LocalDateTime startAt = LocalDateTime.of(20, 12, 20, 0, 0, 0);
        LocalDateTime endAt = LocalDateTime.of(20, 12, 21, 0, 0, 0);
        int maxRound = 16;
        List<Long> teamIds = List.of(1L, 2L, 3L);

        return Stream.of(
                leagueUpdateRequestArgument(
                        "이름이 빈 값이 아닐 경우, 대회 이름, 시작 시간, 종료 시간, 총 라운드 수를 수정한다.",
                        new LeagueRequestDto.Update(newName, startAt, endAt, maxRound, teamIds),
                        List.of(
                                (league) -> () -> assertThat(league.getName()).isEqualTo(newName),
                                (league) -> () -> assertThat(league.getStartAt()).isEqualTo(startAt),
                                (league) -> () -> assertThat(league.getEndAt()).isEqualTo(endAt),
                                (league) -> () -> assertThat(league.getMaxRound().getNumber()).isEqualTo(maxRound)
                        )
                ),
                leagueUpdateRequestArgument(
                        "이름이 빈 값인 경우, 이름을 제외한 시작 시간, 종료 시간, 총 라운드 수를 수정한다.",
                        new LeagueRequestDto.Update(emptyName, startAt, endAt, maxRound, teamIds),
                        List.of(
                                (league) -> () -> assertThat(league.getName()).isNotEqualTo(emptyName),
                                (league) -> () -> assertThat(league.getStartAt()).isEqualTo(startAt),
                                (league) -> () -> assertThat(league.getEndAt()).isEqualTo(endAt),
                                (league) -> () -> assertThat(league.getMaxRound().getNumber()).isEqualTo(maxRound)
                        )
                ),
                leagueUpdateRequestArgument(
                        "팀 목록이 null이어도 다른 정보는 정상적으로 수정된다.",
                        new LeagueRequestDto.Update(newName, startAt, endAt, maxRound, null),
                        List.of(
                                (league) -> () -> assertThat(league.getName()).isEqualTo(newName),
                                (league) -> () -> assertThat(league.getStartAt()).isEqualTo(startAt),
                                (league) -> () -> assertThat(league.getEndAt()).isEqualTo(endAt),
                                (league) -> () -> assertThat(league.getMaxRound().getNumber()).isEqualTo(maxRound)
                        )
                ),
                leagueUpdateRequestArgument(
                        "팀 목록이 비어있어도 다른 정보는 정상적으로 수정된다.",
                        new LeagueRequestDto.Update(newName, startAt, endAt, maxRound, List.of()),
                        List.of(
                                (league) -> () -> assertThat(league.getName()).isEqualTo(newName),
                                (league) -> () -> assertThat(league.getStartAt()).isEqualTo(startAt),
                                (league) -> () -> assertThat(league.getEndAt()).isEqualTo(endAt),
                                (league) -> () -> assertThat(league.getMaxRound().getNumber()).isEqualTo(maxRound)
                        )
                )

        );
    }

    private static Arguments leagueUpdateRequestArgument(
            final String testDisplayName,
            final LeagueRequestDto.Update request,
            final List<Function<League, Executable>> assertions) {
        return Arguments.of(
                named(testDisplayName, request), assertions
        );
    }
}
