package com.sports.server.command.push.exception;

public class PushErrorMessages {
    public static final String DEVICE_NOT_FOUND = "등록되지 않은 기기입니다.";
    public static final String TEAM_FOLLOW_LIMIT_EXCEEDED = "팀은 최대 30개까지 팔로우할 수 있습니다.";
    public static final String GAME_FOLLOW_LIMIT_EXCEEDED = "경기는 최대 30개까지 팔로우할 수 있습니다.";
    public static final String ORGANIZATION_FOLLOW_LIMIT_EXCEEDED = "학교는 최대 5개까지 팔로우할 수 있습니다.";
    public static final String FOLLOW_ORGANIZATION_NOT_FOUND = "존재하지 않는 학교는 팔로우할 수 없습니다.";
    public static final String FOLLOW_TEAM_NOT_FOUND = "존재하지 않는 팀은 팔로우할 수 없습니다.";
    public static final String FOLLOW_GAME_NOT_FOUND = "존재하지 않는 경기는 팔로우할 수 없습니다.";
    public static final String DEVICE_REGISTER_RATE_LIMIT_EXCEEDED = "기기 등록 요청이 너무 많습니다. 잠시 후 다시 시도해주세요.";
}
