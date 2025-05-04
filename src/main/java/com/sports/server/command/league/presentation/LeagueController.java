package com.sports.server.command.league.presentation;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

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
    @ResponseStatus(HttpStatus.OK)
    public void register(@RequestBody final LeagueRequestDto.Register request, Member member) {
        leagueService.register(member, request);
    }

    @DeleteMapping("/{leagueId}")
    @ResponseStatus(HttpStatus.OK)
    public void delete(final Member member, @PathVariable final Long leagueId) {
        leagueService.delete(member, leagueId);
    }

	@PutMapping("/{leagueId}")
    @ResponseStatus(HttpStatus.OK)
	public void update(@PathVariable("leagueId") final Long leagueId,
		@RequestBody final LeagueRequestDto.Update request, Member member) {
		leagueService.update(member, request, leagueId);
	}
}
