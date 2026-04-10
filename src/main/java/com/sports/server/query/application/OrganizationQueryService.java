package com.sports.server.query.application;

import com.sports.server.query.dto.response.OrganizationResponse;
import com.sports.server.query.repository.OrganizationQueryRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrganizationQueryService {

    private final OrganizationQueryRepository organizationQueryRepository;

    public List<OrganizationResponse> findAll() {
        return organizationQueryRepository.findAll().stream()
                .map(OrganizationResponse::new)
                .toList();
    }
}
