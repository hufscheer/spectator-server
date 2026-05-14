package com.sports.server.query.dto.response;

import com.sports.server.command.organization.domain.Organization;

public record OrganizationResponse(
        Long id,
        String name,
        String logoImageUrl,
        boolean isLeagueOngoing
) {
    public OrganizationResponse(Organization organization, boolean isLeagueOngoing) {
        this(organization.getId(), organization.getName(), organization.getLogoImageUrl(), isLeagueOngoing);
    }
}
