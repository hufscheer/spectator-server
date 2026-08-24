package com.sports.server.command.game.dto;

import com.sports.server.command.game.domain.Position;

/**
 * 라인업 선수의 포지션 변경 요청. 포지션은 선택 입력이라 {@code null} 을 보내면 해제된다.
 */
public record LineupPlayerPositionUpdateRequest(
		Position position
) {
}
