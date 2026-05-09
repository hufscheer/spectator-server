package com.sports.server.query.presentation;

import com.sports.server.command.member.domain.Member;
import com.sports.server.common.dto.CursorPageResponse;
import com.sports.server.common.dto.PageRequestDto;
import com.sports.server.query.application.CheerTalkQueryService;
import com.sports.server.query.dto.response.CheerTalkResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CheerTalkQueryController {

    private final CheerTalkQueryService cheerTalkQueryService;

    @GetMapping("/games/{gameId}/cheer-talks")
    public ResponseEntity<CursorPageResponse<CheerTalkResponse.ForSpectator>> getAllCheerTalks(
            @PathVariable final Long gameId,
            @ModelAttribute final PageRequestDto pageRequest) {
        return ResponseEntity.ok(cheerTalkQueryService.getCheerTalksByGameId(gameId, pageRequest));
    }

    @GetMapping("/cheer-talks/reported")
    public ResponseEntity<CursorPageResponse<CheerTalkResponse.ForManager>> getAllReportedCheerTalks(
            @ModelAttribute final PageRequestDto pageRequest, Member member) {
        return ResponseEntity.ok(cheerTalkQueryService.getReportedCheerTalksByAdmin(pageRequest, member));
    }

    @GetMapping("/cheer-talks")
    public ResponseEntity<CursorPageResponse<CheerTalkResponse.ForManager>> getUnblockedCheerTalksByAdmin(
            @ModelAttribute final PageRequestDto pageRequest, Member member) {
        return ResponseEntity.ok(cheerTalkQueryService.getUnblockedCheerTalksByAdmin(pageRequest, member));
    }

    @GetMapping("/cheer-talks/blocked")
    public ResponseEntity<CursorPageResponse<CheerTalkResponse.ForManager>> getAllBlockedCheerTalksByAdmin(
        @ModelAttribute final PageRequestDto pageable, Member member) {
        return ResponseEntity.ok(cheerTalkQueryService.getBlockedCheerTalksByAdmin(pageable, member));
    }

    @GetMapping("/leagues/{leagueId}/cheer-talks/reported")
    public ResponseEntity<CursorPageResponse<CheerTalkResponse.ForManager>> getReportedCheerTalksByLeagueId(
            @PathVariable final Long leagueId,
            @ModelAttribute final PageRequestDto pageRequest, Member member) {
        return ResponseEntity.ok(cheerTalkQueryService.getReportedCheerTalksByLeagueId(leagueId, pageRequest, member));
    }

    @GetMapping("/leagues/{leagueId}/cheer-talks")
    public ResponseEntity<CursorPageResponse<CheerTalkResponse.ForManager>> getUnblockedCheerTalksByLeagueId(
            @PathVariable final Long leagueId,
            @ModelAttribute final PageRequestDto pageRequest, Member member) {
        return ResponseEntity.ok(cheerTalkQueryService.getUnblockedCheerTalksByLeagueId(leagueId, pageRequest, member));
    }

    @GetMapping("/leagues/{leagueId}/cheer-talks/blocked")
    public ResponseEntity<CursorPageResponse<CheerTalkResponse.ForManager>> getBlockedCheerTalksByLeagueId(
            @PathVariable final Long leagueId,
            @ModelAttribute final PageRequestDto pageRequest, Member member) {
        return ResponseEntity.ok(cheerTalkQueryService.getBlockedCheerTalksByLeagueId(leagueId, pageRequest, member));
    }

    @GetMapping("/games/{gameId}/cheer-talks/reported")
    public ResponseEntity<CursorPageResponse<CheerTalkResponse.ForManager>> getReportedCheerTalksByGameId(
            @PathVariable final Long gameId,
            @ModelAttribute final PageRequestDto pageRequest, Member member) {
        return ResponseEntity.ok(cheerTalkQueryService.getReportedCheerTalksByGameId(gameId, pageRequest, member));
    }

    @GetMapping("/games/{gameId}/cheer-talks/blocked")
    public ResponseEntity<CursorPageResponse<CheerTalkResponse.ForManager>> getBlockedCheerTalksByGameId(
            @PathVariable final Long gameId,
            @ModelAttribute final PageRequestDto pageRequest, Member member) {
        return ResponseEntity.ok(cheerTalkQueryService.getBlockedCheerTalksByGameId(gameId, pageRequest, member));
    }
}
