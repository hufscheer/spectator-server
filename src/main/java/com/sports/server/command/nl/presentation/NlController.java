package com.sports.server.command.nl.presentation;

import com.sports.server.command.member.domain.Member;
import com.sports.server.command.nl.application.NlService;
import com.sports.server.command.nl.dto.NlCheckDuplicatesRequest;
import com.sports.server.command.nl.dto.NlCheckDuplicatesResponse;
import com.sports.server.command.nl.dto.NlExecuteRequest;
import com.sports.server.command.nl.dto.NlExecuteResponse;
import com.sports.server.command.nl.dto.NlParseRequest;
import com.sports.server.command.nl.dto.NlParseResponse;
import com.sports.server.command.nl.dto.NlProcessRequest;
import com.sports.server.command.nl.dto.NlProcessResponse;
import com.sports.server.command.nl.dto.NlRegisterTeamRequest;
import com.sports.server.command.nl.dto.NlRegisterTeamResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/nl")
@RequiredArgsConstructor
public class NlController {

    private final NlService nlService;

    @PostMapping("/process")
    public ResponseEntity<NlProcessResponse> process(@Valid @RequestBody NlProcessRequest request, Member member) {
        return ResponseEntity.ok(nlService.process(request, member));
    }

    @PostMapping("/parse")
    public ResponseEntity<NlParseResponse> parse(@Valid @RequestBody NlParseRequest request, Member member) {
        return ResponseEntity.ok(nlService.parse(request, member));
    }

    @PostMapping("/register-team")
    public ResponseEntity<NlRegisterTeamResponse> registerTeamWithPlayers(
            @Valid @RequestBody NlRegisterTeamRequest request, Member member) {
        return ResponseEntity.ok(nlService.registerTeamWithPlayers(request, member));
    }

    @PostMapping("/check-duplicates")
    public ResponseEntity<NlCheckDuplicatesResponse> checkDuplicates(
            @Valid @RequestBody NlCheckDuplicatesRequest request) {
        return ResponseEntity.ok(nlService.checkDuplicates(request));
    }

    @PostMapping("/execute")
    public ResponseEntity<NlExecuteResponse> execute(@Valid @RequestBody NlExecuteRequest request, Member member) {
        return ResponseEntity.ok(nlService.execute(request, member));
    }
}
