package com.sports.server.command.timeline.exception;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TimelineErrorMessage {

    public static final String GAME_ALREADY_FINISHED = "종료된 경기에는 기록을 추가할 수 없어요.";
    public static final String INVALID_PROGRESS_TRANSITION = "지금 경기 상태에서는 할 수 없는 진행이에요.";
    public static final String INVALID_BASKETBALL_SCORE = "농구 점수는 1점, 2점, 3점만 가능해요.";
    public static final String TIMELINE_NOT_FOUND = "이미 삭제되었거나 존재하지 않는 기록이에요.";
    public static final String GAME_NOT_FOUND = "존재하지 않는 경기예요.";
    public static final String MIDDLE_DELETE_ONLY_WHILE_PLAYING = "종료된 경기는 최근 기록만 삭제할 수 있어요.";
    public static final String PROGRESS_TIMELINE_NOT_LAST = "쿼터 시작·종료 기록은 최근 기록일 때만 삭제할 수 있어요.";
    public static final String REPLACEMENT_PLAYER_HAS_LATER_RECORDS = "교체로 들어오거나 나간 선수의 기록이 뒤에 더 있어요. 그 기록부터 먼저 삭제해 주세요.";
    public static final String INCONSISTENT_PROGRESS_STATE = "경기 상태와 기록이 맞지 않아 삭제할 수 없어요. 새로고침 후 다시 시도해 주세요.";

}
