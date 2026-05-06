package com.sports.server.command.cheertalk.exception;

import com.sports.server.common.exception.CustomException;
import org.springframework.http.HttpStatus;

public class CheerTalkRateLimitException extends CustomException {

    public CheerTalkRateLimitException(String message) {
        super(HttpStatus.TOO_MANY_REQUESTS, message);
    }
}
