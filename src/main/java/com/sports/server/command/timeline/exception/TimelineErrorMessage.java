package com.sports.server.command.timeline.exception;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TimelineErrorMessage {

    public static final String GAME_ALREADY_FINISHED = "종료된 게임에 새로운 타임라인을 등록할 수 없습니다.";

}
