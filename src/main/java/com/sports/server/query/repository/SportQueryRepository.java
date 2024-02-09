package com.sports.server.query.repository;

import com.sports.server.command.record.domain.Record;
import com.sports.server.command.sport.domain.Sport;
import java.util.List;
import org.springframework.data.repository.Repository;

public interface SportQueryRepository extends Repository<Record, Long> {
    List<Sport> findAll();
}
