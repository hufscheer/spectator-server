package com.sports.server.command.sport.domain;

import java.util.Optional;
import org.springframework.data.repository.Repository;

public interface SportRepository extends Repository<Sport, Long> {
    Optional<Sport> findByName(String name);
}
