package com.sports.server.common.exception;

import org.springframework.http.HttpStatus;

public class InternalServerException extends CustomException {
    public InternalServerException(String message) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, message);
    }

    public InternalServerException(String message, Throwable cause) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, message);
        initCause(cause);
    }
}
