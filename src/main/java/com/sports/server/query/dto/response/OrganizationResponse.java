package com.sports.server.query.dto.response;

import com.sports.server.command.organization.domain.Organization;

public record OrganizationResponse(
        Long id,
        String name
) {
    public OrganizationResponse(Organization organization) {
        this(organization.getId(), organization.getName());
    }
}
