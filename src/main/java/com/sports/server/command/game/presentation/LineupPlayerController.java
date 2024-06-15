package com.sports.server.command.game.presentation;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sports.server.command.game.application.LineupPlayerService;
import com.sports.server.command.game.dto.LineupPlayerStateUpdateRequest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/lineup-players")
public class LineupPlayerController {
	private final LineupPlayerService lineupPlayerService;

	@PutMapping("/state")
	public ResponseEntity<Void> updatePlayerState(final @RequestBody @Valid LineupPlayerStateUpdateRequest request) {
		lineupPlayerService.updatePlayerState(request);
		return ResponseEntity.ok().build();
	}
}
