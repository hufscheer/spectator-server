package com.sports.server.command.timeline.exception;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TimelineErrorMessage {

    public static final String GAME_ALREADY_FINISHED = "종료된 게임에 새로운 타임라인을 등록할 수 없습니다.";
    public static final String INVALID_PROGRESS_TRANSITION = "현재 경기 상태에서 허용되지 않는 진행 요청입니다.";
    public static final String INVALID_BASKETBALL_SCORE = "농구 점수는 1, 2, 3 중 하나여야 합니다.";
    public static final String TIMELINE_NOT_FOUND = "존재하지 않는 타임라인입니다.";
    public static final String MIDDLE_DELETE_ONLY_WHILE_PLAYING = "종료된 경기는 마지막 기록만 삭제할 수 있습니다.";
    public static final String PROGRESS_TIMELINE_NOT_LAST = "쿼터 진행 기록은 마지막 기록만 삭제할 수 있습니다.";
    public static final String REPLACEMENT_PLAYER_HAS_LATER_RECORDS = "이후 해당 선수의 기록이 있어 삭제할 수 없습니다. 뒤 기록을 먼저 삭제해주세요.";
    public static final String INCONSISTENT_PROGRESS_STATE = "경기 상태와 진행 기록이 일치하지 않아 삭제할 수 없습니다.";

}
