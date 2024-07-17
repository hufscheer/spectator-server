package com.sports.server.command.league.presentation;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.sports.server.command.league.application.LeagueService;
import com.sports.server.command.league.dto.LeagueDto;
import com.sports.server.command.member.domain.Member;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class LeagueController {
	private final LeagueService leagueService;

	@PostMapping("/leagues")
	public ResponseEntity<Void> register(@RequestBody final LeagueDto.LeagueRequest leagueRequest, Member member) {
		leagueService.register(member, leagueRequest);
		return ResponseEntity.ok().build();
	}
}
