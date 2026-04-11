package com.sports.server.command.nl.exception;

public class NlErrorMessages {
    private static final String PLAYER_INFO_FORMAT_HINT = "'이름 학번(9~10자리) 등번호' 형식으로 입력해주세요. (예: 홍길동 202600001 10)";

    public static final String TEAM_NOT_IN_LEAGUE = "해당 팀은 이 리그에 소속되어 있지 않습니다.";
    public static final String PARSE_FAILED = "선수 정보를 인식하지 못했습니다. " + PLAYER_INFO_FORMAT_HINT;
    public static final String NO_PLAYER_INFO = "입력한 텍스트에서 선수 정보를 찾지 못했습니다. " + PLAYER_INFO_FORMAT_HINT;
    public static final String STUDENT_NUMBER_INVALID = "학번은 9자리 또는 10자리 숫자여야 합니다. 학번을 확인해주세요.";
    public static final String STUDENT_NUMBER_NOT_IN_ORIGINAL = "입력한 텍스트에서 해당 학번을 찾을 수 없습니다. 학번이 정확한지 확인해주세요.";
    public static final String INVALID_PLAYER_NAME = "선수 이름이 유효하지 않습니다. 한글 또는 영문으로 입력해주세요.";
}
