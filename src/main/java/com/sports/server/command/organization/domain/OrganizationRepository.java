package com.sports.server.command.organization.domain;

import java.util.Collection;
import org.springframework.data.repository.Repository;

public interface OrganizationRepository extends Repository<Organization, Long> {

    long countByIdIn(Collection<Long> ids);
}
