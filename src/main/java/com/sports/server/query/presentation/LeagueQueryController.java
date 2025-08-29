package com.sports.server.query.presentation;

import com.sports.server.command.member.domain.Member;
import com.sports.server.common.dto.PageRequestDto;
import com.sports.server.query.application.LeagueQueryService;
import com.sports.server.query.dto.request.LeagueQueryRequestDto;
import com.sports.server.query.dto.response.*;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/leagues")
@RequiredArgsConstructor
public class LeagueQueryController {

    private final LeagueQueryService leagueQueryService;

    @GetMapping
    public ResponseEntity<List<LeagueResponse>> findLeagues(
            @ModelAttribute LeagueQueryRequestDto queryRequestDto,
            @ModelAttribute PageRequestDto pageRequest
    ) {
        return ResponseEntity.ok(leagueQueryService.findLeagues(queryRequestDto, pageRequest));
    }

    @GetMapping("/{leagueId}/teams")
    public ResponseEntity<List<LeagueTeamResponse>> findLeagueTeamsByLeague(
            @PathVariable Long leagueId,
            @RequestParam(required = false) Integer round) {
        return ResponseEntity.ok(leagueQueryService.findTeamsByLeagueRound(leagueId, round));
    }

    @GetMapping("/{leagueId}")
    public ResponseEntity<LeagueDetailResponse> findLeagueDetail(@PathVariable Long leagueId) {
        return ResponseEntity.ok(leagueQueryService.findLeagueDetail(leagueId));
    }

    @GetMapping("/{leagueId}/statistics")
    public ResponseEntity<LeagueStatisticsResponse> findLeagueStatistics(@PathVariable Long leagueId) {
        return ResponseEntity.ok(leagueQueryService.findLeagueStatistic(leagueId));
    }

    @GetMapping("/{leagueId}/top-scorers")
    public ResponseEntity<List<LeagueTopScorerResponse>> findTopScorersByLeague(@PathVariable Long leagueId) {
        return ResponseEntity.ok(leagueQueryService.findTop20ScorersByLeagueId(leagueId));
    }

    @GetMapping("/teams/{leagueTeamId}/players")
    public ResponseEntity<List<PlayerResponse>> findPlayersByLeagueTeam(@PathVariable Long leagueTeamId) {
        return ResponseEntity.ok(leagueQueryService.findPlayersByLeagueTeam(leagueTeamId));
    }

    @GetMapping("/manager")
    public ResponseEntity<List<LeagueResponseWithInProgressGames>> findLeaguesByManager(final Member member) {
        return ResponseEntity.ok(leagueQueryService.findLeaguesByManager(member));
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
