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
    @ResponseStatus(HttpStatus.OK)
    public void register(@RequestBody final LeagueRequest.Register request, Member member) {
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
                       @RequestBody final LeagueRequest.Update request, Member member) {
		leagueService.update(member, request, leagueId);
	}

    // 매니저 서버 ui 확인 필요
    @DeleteMapping("/{leagueId}/teams/{teamId}")
    @ResponseStatus(HttpStatus.OK)
    public void removeTeamFromLeague(@PathVariable Long leagueId,
                                     @PathVariable Long teamId, Member member) {
        leagueService.removeTeamFromLeague(member, leagueId, teamId);
    }

    // 리그에 팀 추가 + 팀선수 중 리그팀 플레이어로 추가할 선수 선택
    @PostMapping("/{leagueId}/teams")
    @ResponseStatus(HttpStatus.CREATED)
    public void registerTeamWithPlayers(@PathVariable final Long leagueId,
                                        @RequestBody final LeagueRequest.TeamAndPlayersRegister request,
                                        final Member manager) {
        leagueService.registerTeamWithPlayers(leagueId, request, manager);
    }

    // 매니저 서버 ui 확인 필요
    @DeleteMapping("/{leagueId}/league-team-players/{leagueTeamPlayerId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removePlayerFromLeagueTeamPlayers(@PathVariable final Long leagueId,
                                                  @PathVariable final Long leagueTeamPlayerId,
                                                  final Member manager) {
        leagueService.removePlayerFromLeagueTeamPlayers(leagueId, leagueTeamPlayerId, manager);
    }
}
