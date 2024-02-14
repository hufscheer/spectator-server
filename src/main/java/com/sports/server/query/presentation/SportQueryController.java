package com.sports.server.query.presentation;

import com.sports.server.query.application.SportQueryService;
import com.sports.server.query.dto.response.SportResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/sports" )
public class SportQueryController {

    private final SportQueryService sportQueryService;

    @GetMapping
    public ResponseEntity<List<SportResponse>> findAll() {
        return ResponseEntity.ok(sportQueryService.findAll());
    }
}