package com.sports.server.command.push.domain;

import java.util.List;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

public interface PushFollowRepository extends Repository<PushFollow, Long> {

    List<PushFollow> saveAll(Iterable<PushFollow> follows);

    List<PushFollow> findAllByDeviceId(Long deviceId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM PushFollow f WHERE f.deviceId = :deviceId")
    void deleteAllByDeviceId(@Param("deviceId") Long deviceId);
}
