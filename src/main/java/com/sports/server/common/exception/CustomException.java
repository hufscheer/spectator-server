package com.sports.server.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class CustomException extends RuntimeException {

    private static final String DEFAULT_EXCEPTION_MESSAGE = "서버 내부에 오류가 발생했습니다.";
    private final HttpStatus status;
    private final String message;

    public CustomException() {
        this.status = HttpStatus.INTERNAL_SERVER_ERROR;
        this.message = DEFAULT_EXCEPTION_MESSAGE;
    }

    public CustomException(HttpStatus status) {
        this.status = status;
        this.message = DEFAULT_EXCEPTION_MESSAGE;
    }

    public CustomException(HttpStatus status, String message1) {
        this.status = status;
        this.message = message1;
    }
}
