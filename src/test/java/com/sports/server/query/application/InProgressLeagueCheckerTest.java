package com.sports.server.query.application;

import com.sports.server.command.league.domain.League;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class InProgressLeagueCheckerTest {

    @Mock
    private League league;

    @BeforeEach
    void init() {
        given(league.getStartAt())
                .willReturn(LocalDateTime.of(2024, 2, 24, 0, 0, 0));
        given(league.getEndAt())
                .willReturn(LocalDateTime.of(2024, 2, 28, 0, 0, 0));
    }

    @ParameterizedTest
    @ValueSource(ints = {24, 25, 26, 27, 28})
    void 리그가_진행_중임을_확인_한다(int dayOfMonth) {
        // given
        LocalDate now = LocalDate.of(2024, 2, dayOfMonth);

        // when
        boolean actual = InProgressLeagueChecker.check(now, league);

        // then
        assertThat(actual).isTrue();
    }

    @ParameterizedTest
    @ValueSource(ints = {23, 29})
    void 리그가_진행_중이_아님을_확인_한다(int dayOfMonth) {
        // given
        LocalDate now = LocalDate.of(2024, 2, dayOfMonth);

        // when
        boolean actual = InProgressLeagueChecker.check(now, league);

        // then
        assertThat(actual).isFalse();
    }
}
