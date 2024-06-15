package com.sports.server.command.game.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sports.server.command.game.domain.LineupPlayerRepository;
import com.sports.server.command.game.domain.LineupPlayerState;
import com.sports.server.command.game.dto.LineupPlayerStateUpdateRequest;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class LineupPlayerService {
	private final LineupPlayerRepository lineupPlayerRepository;

	public void updatePlayerState(final LineupPlayerStateUpdateRequest request) {
		lineupPlayerRepository.updatePlayerState(LineupPlayerState.from(request.state()), request.lineupPlayerIds());
	}
}
