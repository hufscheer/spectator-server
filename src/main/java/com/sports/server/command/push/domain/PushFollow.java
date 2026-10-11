package com.sports.server.command.push.domain;

import com.sports.server.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "push_follows")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PushFollow extends BaseEntity<PushFollow> {

    @Column(name = "device_id", nullable = false)
    private Long deviceId;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false)
    private FollowTargetType targetType;

    @Column(name = "target_id", nullable = false)
    private Long targetId;

    public PushFollow(Long deviceId, FollowTargetType targetType, Long targetId) {
        this.deviceId = deviceId;
        this.targetType = targetType;
        this.targetId = targetId;
    }
}
