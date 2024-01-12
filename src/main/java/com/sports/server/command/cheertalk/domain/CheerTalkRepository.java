package com.sports.server.command.cheertalk.domain;


import org.springframework.data.repository.Repository;

public interface CheerTalkRepository extends Repository<CheerTalk, Long> {
    void save(CheerTalk cheerTalk);
}
