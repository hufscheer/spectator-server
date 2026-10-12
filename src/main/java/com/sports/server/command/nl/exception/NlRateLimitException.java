package com.sports.server.command.nl.exception;

import com.sports.server.common.exception.CustomException;
import org.springframework.http.HttpStatus;

public class NlRateLimitException extends CustomException {

    public NlRateLimitException(String message) {
        super(HttpStatus.TOO_MANY_REQUESTS, message);
    }
}
