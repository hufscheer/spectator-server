package com.sports.server.command.nl.exception;

public class NlErrorMessages {
    public static final String TEAM_NOT_IN_LEAGUE = "해당 팀은 이 리그에 소속되어 있지 않습니다.";
    public static final String PARSE_FAILED = "선수 정보를 파싱할 수 없습니다. 이름과 학번(9자리)을 포함하여 다시 입력해주세요.";
    public static final String NO_PLAYER_INFO = "선수 정보를 찾을 수 없습니다. 다시 입력해주세요.";
    public static final String STUDENT_NUMBER_INVALID = "학번이 9자리가 아닙니다";
    public static final String STUDENT_NUMBER_NOT_IN_ORIGINAL = "원본 텍스트에서 해당 학번을 찾을 수 없습니다";
    public static final String INVALID_PLAYER_NAME = "선수 이름이 유효하지 않습니다";
}
