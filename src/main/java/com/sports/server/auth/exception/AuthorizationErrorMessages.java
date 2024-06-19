package com.sports.server.auth.exception;

public class AuthorizationErrorMessages {
    public static final String MEMBER_NOT_FOUND_EXCEPTION = "존재하지 않는 사용자입니다.";
    public static final String PERMISSION_DENIED = "권한이 없습니다.";
    public static final String TOKEN_EXPIRED_EXCEPTION = "토큰이 만료되었습니다. 다시 인증해주세요.";
    public static final String INVALID_TOKEN_EXCEPTION = "제공된 토큰이 유효하지 않습니다. 다시 인증해주세요.";
}
