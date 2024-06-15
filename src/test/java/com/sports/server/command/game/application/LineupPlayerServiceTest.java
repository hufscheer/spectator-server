package com.sports.server.command.game.application;

import static org.assertj.core.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import com.sports.server.command.game.dto.LineupPlayerStateUpdateRequest;
import com.sports.server.command.game.exception.LineupErrorMessages;
import com.sports.server.common.exception.CustomException;
import com.sports.server.support.ServiceTest;

public class LineupPlayerServiceTest extends ServiceTest {
	@Autowired
	private LineupPlayerService lineupPlayerService;

	@Test
	void 존재하지_않는_라인업_선수_상태로_변경하려고_하면_예외가_발생한다() {
		//given
		LineupPlayerStateUpdateRequest request = new LineupPlayerStateUpdateRequest("WOUNDED", List.of(1L, 2L));

		//when & then
		assertThatThrownBy(
			() -> lineupPlayerService.updatePlayerState(request)
		).isInstanceOf(CustomException.class)
			.hasMessage(LineupErrorMessages.STATE_NOT_FOUND_EXCEPTION)
			.extracting("status").isEqualTo(HttpStatus.BAD_REQUEST);
	}
}
