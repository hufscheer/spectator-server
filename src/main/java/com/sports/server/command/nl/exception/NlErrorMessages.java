package com.sports.server.command.nl.exception;

public class NlErrorMessages {
    private static final String PLAYER_INFO_FORMAT_HINT = "'이름 학번(9~10자리) 등번호' 형식으로 입력해주세요. (예: 홍길동 202600001 10)";

    public static final String TEAM_NOT_IN_LEAGUE = "해당 팀은 이 리그에 소속되어 있지 않습니다.";
    public static final String PARSE_FAILED = "선수 정보를 인식하지 못했습니다. " + PLAYER_INFO_FORMAT_HINT;
    public static final String NO_PLAYER_INFO = "입력한 텍스트에서 선수 정보를 찾지 못했습니다. " + PLAYER_INFO_FORMAT_HINT;
    public static final String STUDENT_NUMBER_INVALID = "학번은 9자리 또는 10자리 숫자여야 합니다. 학번을 확인해주세요.";
    public static final String STUDENT_NUMBER_NOT_IN_ORIGINAL = "입력한 텍스트에서 해당 학번을 찾을 수 없습니다. 학번이 정확한지 확인해주세요.";
    public static final String INVALID_PLAYER_NAME = "선수 이름이 유효하지 않습니다. 한글 또는 영문으로 입력해주세요.";

    public static final String EXTRACT_UNSUPPORTED_TYPE = "사진(JPG·PNG·WEBP·HEIC), 엑셀(xlsx), CSV, PDF 만 올릴 수 있습니다.";
    public static final String EXTRACT_LEGACY_XLS = "엑셀 파일은 xlsx 로 저장해서 올려 주세요.";
    public static final String EXTRACT_EMPTY_FILE = "빈 파일입니다. 내용이 있는 파일을 올려 주세요.";
    public static final String EXTRACT_FILE_TOO_LARGE = "파일은 10MB 까지 올릴 수 있습니다.";
    public static final String EXTRACT_NOTHING_READ = "파일에서 명단을 읽지 못했습니다.";
    public static final String EXTRACT_RATE_LIMIT_EXCEEDED = "파일 읽기 요청이 너무 많습니다. 잠시 후 다시 시도해주세요.";
    public static final String EXTRACT_AI_UNAVAILABLE = "AI 서비스가 일시적으로 응답하지 않습니다. 잠시 후 다시 시도해주세요.";
}
