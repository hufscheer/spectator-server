package com.sports.server.command.cheertalk.domain;

import org.springframework.data.repository.Repository;

public interface PendingCheerTalkRepository extends Repository<PendingCheerTalk, Long> {
    void save(PendingCheerTalk pendingCheerTalk);
}
