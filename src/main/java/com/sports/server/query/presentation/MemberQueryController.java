package com.sports.server.query.presentation;

import com.sports.server.command.member.domain.Member;
import com.sports.server.query.application.MemberQueryService;
import com.sports.server.query.dto.response.MemberResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/members")
public class MemberQueryController {

    private final MemberQueryService memberQueryService;

    @GetMapping("/info")
    public ResponseEntity<MemberResponse> getMember(Member member) {
        return ResponseEntity.ok(memberQueryService.getMemberInfo(member));
    }

}
