package com.sports.server.command.team.presentation;

import com.sports.server.command.team.application.TeamService;
import com.sports.server.command.team.dto.TeamRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class TeamController {
    private final TeamService teamService;

    @PostMapping("/teams")
    @ResponseStatus(HttpStatus.CREATED)
    public void register(@RequestBody TeamRequest.Register request) {
        teamService.register(request);
    }

    @PatchMapping("/teams/{teamId}")
    @ResponseStatus(HttpStatus.OK)
    public void update(@RequestBody TeamRequest.Update request, @PathVariable Long teamId) {
        teamService.update(request, teamId);
    }

    @DeleteMapping("/teams/{teamId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long teamId) {
        teamService.delete(teamId);
    }

    @PostMapping("/teams/{teamId}/delete-logo")
    @ResponseStatus(HttpStatus.OK)
    public void deleteLogo(@PathVariable Long teamId) {
        teamService.deleteLogoImage(teamId);
    }

    @PostMapping("/teams/{teamId}/players")
    @ResponseStatus(HttpStatus.OK)
    public void addPlayersToTeam(@PathVariable Long teamId,
                          @RequestBody List<TeamRequest.TeamPlayerRegister> request){
        teamService.addPlayersToTeam(teamId, request);
    }

    @DeleteMapping("/team-players/{teamPlayerId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTeamPlayer(@PathVariable Long teamPlayerId){
        teamService.deleteTeamPlayer(teamPlayerId);
    }

}
