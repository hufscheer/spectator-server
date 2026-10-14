package com.sports.server.command.push.domain;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

public interface PushDeviceRepository extends Repository<PushDevice, Long> {

    PushDevice save(PushDevice device);

    void delete(PushDevice device);

    Optional<PushDevice> findByDeviceKey(String deviceKey);

    Optional<PushDevice> findByTokenHash(String tokenHash);

    List<PushDevice> findAllById(Iterable<Long> ids);

    // 유니크 제약이 걸린 토큰을 옮기기 전에 먼저 지워야 하므로 즉시 실행한다
    @Modifying(flushAutomatically = true)
    @Query("DELETE FROM PushDevice d WHERE d.tokenHash = :tokenHash AND d.id <> :deviceId")
    void deleteOtherOwnersOfToken(@Param("tokenHash") String tokenHash, @Param("deviceId") Long deviceId);

    @Query("SELECT d FROM PushDevice d WHERE d.id IN ("
            + "SELECT f.deviceId FROM PushFollow f "
            + "WHERE (f.targetType = com.sports.server.command.push.domain.FollowTargetType.TEAM AND f.targetId IN :teamIds) "
            + "OR (f.targetType = com.sports.server.command.push.domain.FollowTargetType.GAME AND f.targetId = :gameId))")
    List<PushDevice> findFollowers(@Param("teamIds") Collection<Long> teamIds, @Param("gameId") Long gameId);
}
