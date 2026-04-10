package com.sports.server.query.presentation;

import com.sports.server.query.application.OrganizationQueryService;
import com.sports.server.query.dto.response.OrganizationResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/organizations")
@RequiredArgsConstructor
public class OrganizationQueryController {

    private final OrganizationQueryService organizationQueryService;

    @GetMapping
    public ResponseEntity<List<OrganizationResponse>> findAll() {
        return ResponseEntity.ok(organizationQueryService.findAll());
    }
}
