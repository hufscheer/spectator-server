package com.sports.server.query.repository;

import com.sports.server.command.organization.domain.Organization;
import java.util.List;
import org.springframework.data.repository.Repository;

public interface OrganizationQueryRepository extends Repository<Organization, Long> {

    List<Organization> findAll();
}
