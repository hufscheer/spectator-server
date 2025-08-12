package com.sports.server.command.league.presentation;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import com.sports.server.command.league.application.LeagueService;
import com.sports.server.command.league.dto.LeagueRequest;
import com.sports.server.command.member.domain.Member;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/leagues")
public class LeagueController {
	private final LeagueService leagueService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void register(@RequestBody final LeagueRequest.Register request, Member member) {
        leagueService.register(member, request);
    }

    @DeleteMapping("/{leagueId}")
    @ResponseStatus(HttpStatus.OK)
    public void delete(@PathVariable("leagueId") final Long leagueId, final Member member) {
        leagueService.delete(member, leagueId);
    }

	@PutMapping("/{leagueId}")
    @ResponseStatus(HttpStatus.OK)
	public void update(@PathVariable("leagueId") final Long leagueId,
                       @RequestBody final LeagueRequest.Update request, Member member) {
		leagueService.update(member, request, leagueId);
	}

    @PostMapping("/{leagueId}/teams")
    @ResponseStatus(HttpStatus.CREATED)
    public void addTeamsToLeague(@PathVariable("leagueId") final Long leagueId,
                                 @RequestBody final LeagueRequest.Teams request, final Member member) {
        leagueService.addTeams(member, leagueId, request);
    }

    @DeleteMapping("/{leagueId}/teams")
    @ResponseStatus(HttpStatus.OK)
    public void removeTeamsFromLeague(@PathVariable("leagueId") final Long leagueId,
                                     @RequestBody final LeagueRequest.Teams request, final Member member) {
        leagueService.removeTeams(member, leagueId, request);
    }

}
