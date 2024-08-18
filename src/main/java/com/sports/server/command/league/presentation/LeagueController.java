package com.sports.server.command.league.presentation;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sports.server.command.league.application.LeagueService;
import com.sports.server.command.league.dto.LeagueRequestDto;
import com.sports.server.command.member.domain.Member;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/leagues")
public class LeagueController {
	private final LeagueService leagueService;

    @PostMapping
    public ResponseEntity<Void> register(@RequestBody final LeagueRequestDto.Register request, Member member) {
        leagueService.register(member, request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{leagueId}")
    public ResponseEntity<Void> delete(final Member member, @PathVariable final Long leagueId) {
        leagueService.delete(member, leagueId);
        return ResponseEntity.ok().build();
    }

	@PutMapping("/{leagueId}")
	public ResponseEntity<Void> update(@PathVariable("leagueId") final Long leagueId,
		@RequestBody final LeagueRequestDto.Update request, Member member) {
		leagueService.update(member, request, leagueId);
		return ResponseEntity.ok().build();
	}
}
