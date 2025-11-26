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

    // Player 관련
    public static final String PLAYER_STUDENT_NUMBER_DUPLICATE = "이미 존재하는 학번입니다.";

    // League 관련
    public static final String LEAGUE_ROUND_EXCEEDS_MAX = "최대 라운드보다 더 큰 라운드의 경기를 등록할 수 없습니다.";

    // GameTeam 관련
    public static final String GAME_TEAM_INVALID_CHEER_COUNT_RANGE = "잘못된 범위의 응원 요청 횟수입니다.";
    public static final String GAME_TEAM_CHEER_COUNT_LIMIT_EXCEEDED = "총 응원 횟수가 한계에 도달했습니다.";
    public static final String GAME_TEAM_PLAYER_NOT_IN_TEAM = "해당 게임팀에 속하지 않는 선수입니다.";
    public static final String GAME_TEAM_CAPTAIN_ALREADY_EXISTS = "이미 등록된 주장이 존재합니다.";

    // LineupPlayer 관련
    public static final String LINEUP_PLAYER_ALREADY_STARTER = "이미 선발로 등록된 선수입니다.";
    public static final String LINEUP_PLAYER_ALREADY_CANDIDATE = "이미 후보로 등록된 선수입니다.";
    public static final String LINEUP_PLAYER_ALREADY_CAPTAIN = "이미 주장으로 등록된 선수입니다.";
    public static final String LINEUP_PLAYER_NOT_CAPTAIN = "해당 선수는 주장이 아닙니다.";

    // GameService 관련
    public static final String GAME_SERVICE_DUPLICATE_PLAYER_IDS = "요청에 중복된 선수 ID가 포함되어 있습니다.";

    // CheerTalkService 관련
    public static final String CHEER_TALK_GAME_TEAM_NOT_FOUND = "존재하지 않는 팀에 대한 응원톡 등록 요청입니다";

}
