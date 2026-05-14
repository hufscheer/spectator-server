package com.sports.server.query.application;

import com.sports.server.query.dto.response.OrganizationResponse;
import com.sports.server.query.repository.LeagueQueryRepository;
import com.sports.server.query.repository.OrganizationQueryRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrganizationQueryService {

    private final OrganizationQueryRepository organizationQueryRepository;
    private final LeagueQueryRepository leagueQueryRepository;

    public List<OrganizationResponse> findAll() {
        Set<Long> ongoingOrganizationIds = Set.copyOf(
                leagueQueryRepository.findOrganizationIdsWithOngoingLeague(LocalDateTime.now())
        );
        return organizationQueryRepository.findAll().stream()
                .map(org -> new OrganizationResponse(org, ongoingOrganizationIds.contains(org.getId())))
                .toList();
    }
}
