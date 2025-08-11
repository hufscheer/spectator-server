package com.sports.server.command.team.presentation;

import com.sports.server.command.team.application.TeamService;
import com.sports.server.command.team.dto.TeamRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/teams")
public class TeamController {
    private final TeamService teamService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void register(@RequestBody TeamRequest.Register request) {
        teamService.register(request);
    }

    @PutMapping("/{teamId}")
    @ResponseStatus(HttpStatus.OK)
    public void update(@RequestBody TeamRequest.Update request, @PathVariable Long teamId) {
        teamService.update(request, teamId);
    }

    @DeleteMapping("/{teamId}")
    @ResponseStatus(HttpStatus.OK)
    public void delete(@PathVariable Long teamId) {
        teamService.delete(teamId);
    }

    @PostMapping("/{teamId}/delete-logo")
    @ResponseStatus(HttpStatus.OK)
    public void deleteLogo(@PathVariable Long teamId) {
        teamService.deleteLogoImage(teamId);
    }

    @PostMapping("/{teamId}/players")
    @ResponseStatus(HttpStatus.OK)
    public void addPlayersToTeam(@PathVariable Long teamId,
                          @RequestBody List<TeamRequest.TeamPlayerRegister> request){
        teamService.addPlayersToTeam(teamId, request);
    }

    @DeleteMapping("/{teamId}/players/{playerId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePlayerFromTeam(@PathVariable Long teamId,
                                     @PathVariable Long playerId){
        teamService.deletePlayerFromTeam(teamId, playerId);
    }

}
