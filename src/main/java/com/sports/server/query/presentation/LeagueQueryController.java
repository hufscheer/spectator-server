package com.sports.server.query.presentation;

import com.sports.server.command.member.domain.Member;
import com.sports.server.query.application.LeagueQueryService;
import com.sports.server.query.dto.response.*;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/leagues")
@RequiredArgsConstructor
public class LeagueQueryController {

    private final LeagueQueryService leagueQueryService;

    @GetMapping
    public ResponseEntity<List<LeagueResponse>> findLeagues(@RequestParam(required = false) Integer year) {
        return ResponseEntity.ok(leagueQueryService.findLeagues(year));
    }

    @GetMapping("/{leagueId}/sports")
    public ResponseEntity<List<LeagueSportResponse>> findSportsByLeague(@PathVariable Long leagueId) {
        return ResponseEntity.ok(leagueQueryService.findSportsByLeague(leagueId));
    }

    @GetMapping("/{leagueId}/teams")
    public ResponseEntity<List<LeagueTeamResponse>> findLeagueTeamsByLeague(
            @PathVariable Long leagueId,
            @RequestParam(required = false) String descriptionOfRound) {
        return ResponseEntity.ok(leagueQueryService.findTeamsByLeagueRound(leagueId, descriptionOfRound));
    }

    @GetMapping("/{leagueId}")
    public ResponseEntity<LeagueDetailResponse> findLeagueDetail(@PathVariable Long leagueId) {
        return ResponseEntity.ok(leagueQueryService.findLeagueDetail(leagueId));
    }

    @GetMapping("/teams/{leagueTeamId}/players")
    public ResponseEntity<List<LeagueTeamPlayerResponse>> findPlayersByLeagueTeam(@PathVariable Long leagueTeamId) {
        return ResponseEntity.ok(leagueQueryService.findPlayersByLeagueTeam(leagueTeamId));
    }

    @GetMapping("/manager")
    public ResponseEntity<List<LeagueResponseWithInProgressGames>> findLeaguesByManager(final Member member) {
        return ResponseEntity.ok(leagueQueryService.findLeaguesByManager(member));
    }

    @GetMapping("/teams/{leagueTeamId}")
    public ResponseEntity<LeagueTeamDetailResponse> findLeagueTeam(@PathVariable final Long leagueTeamId) {
        return ResponseEntity.ok(leagueQueryService.findLeagueTeam(leagueTeamId));
    }

    @GetMapping("/{leagueId}/games")
    public ResponseEntity<LeagueResponseWithGames> findLeagueAndGames(@PathVariable final Long leagueId) {
        return ResponseEntity.ok(leagueQueryService.findLeagueAndGames(leagueId));
    }

    @GetMapping("/manager/manage")
    public ResponseEntity<List<LeagueResponseToManage>> findLeaguesByManagerToManage(final Member manager) {
        return ResponseEntity.ok(leagueQueryService.findLeaguesByManagerToManage(manager));
    }
}
