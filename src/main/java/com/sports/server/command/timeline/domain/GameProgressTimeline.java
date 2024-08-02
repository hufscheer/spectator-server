package com.sports.server.command.timeline.domain;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@DiscriminatorValue("GAME_PROGRESS")
@Getter
public class GameProgressTimeline extends Timeline  {

    @Enumerated(EnumType.STRING)
    @Column(name = "game_progress_type")
    private GameProgressType gameProgressType;

    @Override
    public String getType() {
        return "GAME_PROGRESS";
    }

    @Override
    public void rollback() {
        // TODO 게임 상태 변경 타임라인 생성 로직 구현 이후 구현 예정
    }
}
