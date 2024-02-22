package com.sports.server.query.presentation;

import com.sports.server.query.application.LeagueQueryService;
import com.sports.server.query.dto.response.LeagueResponse;
import com.sports.server.query.dto.response.LeagueSportResponse;
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
}
