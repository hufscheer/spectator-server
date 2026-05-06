package com.sports.server.command.game.exception;

import com.sports.server.common.exception.CustomException;
import org.springframework.http.HttpStatus;

public class CheerCountRateLimitException extends CustomException {

    public CheerCountRateLimitException(String message) {
        super(HttpStatus.TOO_MANY_REQUESTS, message);
    }
}
