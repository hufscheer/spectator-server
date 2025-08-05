package com.sports.server.command.team.presentation;

import com.sports.server.command.team.application.TeamService;
import com.sports.server.command.team.dto.TeamRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/teams")
public class TeamController {
    private final TeamService teamService;

    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    public void register(@RequestBody TeamRequest.Register request) {
        teamService.register(request);
    }

    @PatchMapping("/{teamId}")
    @ResponseStatus(HttpStatus.OK)
    public void update(@RequestBody TeamRequest.Update request, @PathVariable Long teamId) {
        teamService.update(request, teamId);
    }

    @PostMapping("/{teamId}/delete-logo")
    @ResponseStatus(HttpStatus.OK)
    public void deleteLogo(@PathVariable Long teamId) {
        teamService.deleteLogoImage(teamId);
    }

    @PostMapping("/{teamId}/player")
    @ResponseStatus(HttpStatus.OK)
    public void addPlayerToTeam(@PathVariable Long teamId,
                          @RequestBody TeamRequest.PlayerIdRequest request){
        teamService.addPlayerToTeam(teamId, request);
    }

    @DeleteMapping("/{teamId}/player")
    @ResponseStatus(HttpStatus.OK)
    public void deletePlayerFromTeam(@PathVariable Long teamId,
                                     @RequestBody TeamRequest.PlayerIdRequest request){
        teamService.deletePlayerFromTeam(teamId, request);
    }

}
