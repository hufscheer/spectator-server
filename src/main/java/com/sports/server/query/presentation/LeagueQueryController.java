package com.sports.server.query.presentation;

import com.sports.server.query.application.LeagueQueryService;
import com.sports.server.query.dto.response.LeagueDetailResponse;
import com.sports.server.query.dto.response.LeagueResponse;
import com.sports.server.query.dto.response.LeagueSportResponse;
import com.sports.server.query.dto.response.LeagueTeamPlayerResponse;
import com.sports.server.query.dto.response.LeagueTeamResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public ResponseEntity<List<LeagueTeamResponse>> findLeagueTeamsByLeague(@PathVariable Long leagueId) {
        return ResponseEntity.ok(leagueQueryService.findTeamsByLeague(leagueId));
    }

    @GetMapping("/{leagueId}")
    public ResponseEntity<LeagueDetailResponse> findLeagueDetail(@PathVariable Long leagueId) {
        return ResponseEntity.ok(leagueQueryService.findLeagueDetail(leagueId));
    }

    @GetMapping("/teams/{leagueTeamId}/players/all")
    public ResponseEntity<List<LeagueTeamPlayerResponse>> findPlayersByLeagueTeam(@PathVariable Long leagueTeamId) {
        return ResponseEntity.ok(leagueQueryService.findPlayersByLeagueTeam(leagueTeamId));
    }
}
