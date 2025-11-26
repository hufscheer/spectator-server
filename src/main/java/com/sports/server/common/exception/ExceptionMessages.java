package com.sports.server.common.exception;

public class ExceptionMessages {

    public static final String GAME_NOT_FOUND_EXCEPTION = "해당 경기는 존재하지 않습니다.";
    public static final String GAME_TEAM_NOT_FOUND_EXCEPTION = "해당 경기의 팀은 존재하지 않습니다.";

    // Timeline 관련
    public static final String INVALID_RECORDED_AT = "시간은 0 이상이어야 합니다.";

    // Quarter 관련
    public static final String QUARTER_NOT_FOUND_BY_NAME = "해당 쿼터 이름이 존재하지 않습니다: %s";
    public static final String QUARTER_ID_NULL = "쿼터 아이디는 null이 될 수 없습니다.";
    public static final String QUARTER_NOT_FOUND_BY_ID = "해당 아이디의 쿼터가 존재하지 않습니다: %d";

    // Player 관련
    public static final String INVALID_PLAYER_SUBSTITUTION = "다른 팀의 선수끼리 교체할 수 없습니다.";

    // CheerTalk 관련
    public static final String CHEERTALK_SERIALIZATION_FAILED = "CheerTalk을 JSON으로 변환하는데 실패했습니다.";
    public static final String CHEERTALK_DESERIALIZATION_FAILED = "JSON을 CheerTalk으로 변환하는데 실패했습니다.";

    // Log Filter 관련
    public static final String LOG_FILTER_ERROR = "요청 처리 중 오류가 발생했습니다.";

}
