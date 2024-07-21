package com.sports.server.command.timeline.domain;

import org.springframework.data.repository.Repository;

public interface TimelineRepository extends Repository<Timeline, Long> {
    void save(Timeline timeline);
}
