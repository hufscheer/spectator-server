package com.sports.server.command.team.presentation;

import com.sports.server.command.team.application.TeamService;
import com.sports.server.command.team.dto.TeamRequest;
import com.sports.server.command.member.domain.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/leagues/{leagueId}/teams")
public class LeagueTeamController {
    private final TeamService leagueTeamService;

    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    public void register(@PathVariable Long leagueId, @RequestBody TeamRequest.Register request,
                                         Member member) {
        leagueTeamService.register(leagueId, member, request);
    }

    @PatchMapping("/{teamId}")
    @ResponseStatus(HttpStatus.OK)
    public void update(@PathVariable Long leagueId, @RequestBody TeamRequest.Update request,
                                       Member member, @PathVariable Long teamId) {
        leagueTeamService.update(leagueId, request, member, teamId);
    }

    @PostMapping("/{teamId}/delete-logo")
    @ResponseStatus(HttpStatus.OK)
    public void deleteLogo(@PathVariable Long leagueId, Member member, @PathVariable Long teamId) {
        leagueTeamService.deleteLogoImage(leagueId, member, teamId);
    }

    @DeleteMapping("/{teamId}")
    @ResponseStatus(HttpStatus.OK)
    public void delete(@PathVariable Long leagueId,
                                       Member member, @PathVariable Long teamId) {
        leagueTeamService.delete(leagueId, member, teamId);
    }
}
