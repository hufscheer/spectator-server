package com.sports.server.command.timeline.exception;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TimelineErrorMessage {

    public static final String GAME_ALREADY_FINISHED = "종료된 게임에 새로운 타임라인을 등록할 수 없습니다.";
    public static final String INVALID_PROGRESS_TRANSITION = "현재 경기 상태에서 허용되지 않는 진행 요청입니다.";

}
