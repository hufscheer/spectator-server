package com.sports.server.command.organization.domain;

import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

public interface OrganizationRepository extends Repository<Organization, Long> {

    long countByIdIn(Collection<Long> ids);

    @Query("SELECT o.id FROM Organization o WHERE o.id IN :ids")
    List<Long> findExistingIds(@Param("ids") Collection<Long> ids);
}
