package com.sports.server.query.presentation;

import com.sports.server.command.member.domain.Member;
import com.sports.server.common.dto.CursorPageResponse;
import com.sports.server.common.dto.PageRequestDto;
import com.sports.server.query.application.PlayerQueryService;
import com.sports.server.query.dto.response.PlayerResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/players")
public class PlayerQueryController {

    private final PlayerQueryService playerQueryService;

    @GetMapping
    public ResponseEntity<CursorPageResponse<PlayerResponse>> getAllPlayers(
            @ModelAttribute PageRequestDto pageRequest,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String studentNumber,
            Member member){
        return ResponseEntity.ok(playerQueryService.getAllPlayers(member, pageRequest, name, studentNumber));
    }

    @GetMapping("/{playerId}")
    public ResponseEntity<PlayerResponse> getPlayerById(@PathVariable Long playerId){
        return ResponseEntity.ok(playerQueryService.getPlayerDetail(playerId));
    }
}
