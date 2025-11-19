package com.sports.server.command.cheertalk.domain;

import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

public interface PendingCheerTalkRepository extends Repository<PendingCheerTalk, Long> {
    void save(PendingCheerTalk pendingCheerTalk);

    @Query("SELECT p FROM PendingCheerTalk p "
            + "ORDER BY p.createdAt ASC")
    List<PendingCheerTalk> findTop20Oldest(Pageable pageable);

    void delete(PendingCheerTalk pendingCheerTalk);
}
