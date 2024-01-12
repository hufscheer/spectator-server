package com.sports.server.command.comment.presentation;

import com.sports.server.command.comment.application.CheerTalkService;
import com.sports.server.command.comment.dto.CheerTalkRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class CheerTalkController {

    private final CheerTalkService cheerTalkService;

    @PostMapping("/cheer-talks")
    public ResponseEntity<Void> register(@RequestBody @Valid final CheerTalkRequest cheerTalkRequest) {
        cheerTalkService.register(cheerTalkRequest);
        return ResponseEntity.ok(null);
    }
}
