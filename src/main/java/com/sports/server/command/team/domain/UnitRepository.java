package com.sports.server.command.team.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UnitRepository extends JpaRepository<Unit, Long> {

    List<Unit> findAllByOrganizationId(Long organizationId);

    Optional<Unit> findByNameAndOrganizationId(String name, Long organizationId);
}