package com.sports.server.command.push.exception;

import com.sports.server.common.exception.CustomException;
import org.springframework.http.HttpStatus;

public class PushRateLimitException extends CustomException {

    public PushRateLimitException(String message) {
        super(HttpStatus.TOO_MANY_REQUESTS, message);
    }
}
