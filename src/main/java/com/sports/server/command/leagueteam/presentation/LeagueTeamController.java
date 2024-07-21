package com.sports.server.command.leagueteam.presentation;

import com.sports.server.command.leagueteam.application.LeagueTeamService;
import com.sports.server.command.leagueteam.dto.LeagueTeamRequest;
import com.sports.server.command.member.domain.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/leagues/{leagueId}/teams")
public class LeagueTeamController {
    private final LeagueTeamService leagueTeamService;

    @PostMapping
    public ResponseEntity<Void> register(@PathVariable Long leagueId, @RequestBody LeagueTeamRequest.Register request,
                                         Member member) {
        leagueTeamService.register(leagueId, member, request);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{teamId}")
    public ResponseEntity<Void> update(@PathVariable Long leagueId, @RequestBody LeagueTeamRequest.Update request,
                                       Member member, @PathVariable Long teamId) {
        leagueTeamService.update(leagueId, request, member, teamId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{teamId}/delete-logo")
    public ResponseEntity<Void> deleteLogo(@PathVariable Long leagueId, Member member, @PathVariable Long teamId) {
        leagueTeamService.deleteLogoImage(leagueId, member, teamId);
        return ResponseEntity.ok().build();
    }

}
