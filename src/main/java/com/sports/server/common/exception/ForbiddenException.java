package com.sports.server.common.exception;

import org.springframework.http.HttpStatus;

/**
 * 로그인은 했지만 그 자원을 다룰 권한이 없을 때 쓴다.
 * 매니저 웹은 401 을 받으면 로그인 만료로 보고 로그아웃시키므로, 권한 거부에는 401 을 쓰지 않는다.
 */
public class ForbiddenException extends CustomException {
    public ForbiddenException(String message) {
        super(HttpStatus.FORBIDDEN, message);
    }
}
