package com.sports.server.command.league.domain;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LeagueProgressTest {
	@Mock
	private League league;

	@BeforeEach
	void init() {
		doReturn(LocalDateTime.of(2024, 2, 24, 0, 0, 0)).when(league).getStartAt();
		lenient().doReturn(LocalDateTime.of(2024, 2, 28, 0, 0, 0)).when(league).getEndAt();
	}

	@ParameterizedTest
	@ValueSource(ints = {22, 23})
	void 리그가_시작_전인지_확인한다(int dayOfMonth) {
		// given
		LocalDateTime now = LocalDateTime.of(2024, 2, dayOfMonth, 0, 0, 0);

		// when
		String actual = LeagueProgress.getProgressDescription(now, league);

		// then
		assertThat(actual).isEqualTo(LeagueProgress.BEFORE_START.getDescription());
	}

	@ParameterizedTest
	@ValueSource(ints = {24, 25, 26, 27})
	void 리그가_진행_중임을_확인_한다(int dayOfMonth) {
		// given
		LocalDateTime now = LocalDateTime.of(2024, 2, dayOfMonth, 0, 0, 0);

		// when
		String actual = LeagueProgress.getProgressDescription(now, league);

		// then
		assertThat(actual).isEqualTo(LeagueProgress.IN_PROGRESS.getDescription());
	}

	@ParameterizedTest
	@ValueSource(ints = {28, 29})
	void 리그가_종료됐는지_확인_한다(int dayOfMonth) {
		// given
		LocalDateTime now = LocalDateTime.of(2024, 2, dayOfMonth, 0, 0, 0);

		// when
		String actual = LeagueProgress.getProgressDescription(now, league);

		// then
		assertThat(actual).isEqualTo(LeagueProgress.FINISHED.getDescription());
	}
}
