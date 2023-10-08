package com.sports.server.game.exception;

import com.sports.server.common.exception.CustomException;
import org.springframework.http.HttpStatus;

public class GameNotFoundException extends CustomException {

    public GameNotFoundException() {
        super(HttpStatus.NOT_FOUND, "해당 경기를 찾을 수 없습니다.");
    }
}