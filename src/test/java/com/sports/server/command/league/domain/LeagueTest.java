package com.sports.server.command.league.domain;

import static com.sports.server.support.fixture.FixtureMonkeyUtils.entityBuilder;
import static com.sports.server.support.fixture.FixtureMonkeyUtils.maxRoundArbitrary;
import static com.sports.server.support.fixture.FixtureMonkeyUtils.nameArbitrary;
import static org.assertj.core.api.Assertions.assertThat;

import com.sports.server.command.member.domain.Member;
import com.sports.server.command.organization.domain.Organization;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class LeagueTest {
    @Nested
    @DisplayName("리그 생성 시")
    class CreateLeague {
        private Member manager;
        private Organization organization;

        @BeforeEach
        void setUp() {
            manager = entityBuilder(Member.class).sample();
            organization = entityBuilder(Organization.class).sample();
        }

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

    @Nested
    @DisplayName("리그 삭제 시")
    class DeleteLeague {
        private Member manager;
        private Organization organization;

        @BeforeEach
        void setUp() {
            manager = entityBuilder(Member.class).sample();
            organization = entityBuilder(Organization.class).sample();
        }
    }
}
