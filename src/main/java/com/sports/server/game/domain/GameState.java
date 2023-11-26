package com.sports.server.game.domain;

import com.sports.server.common.exception.CustomException;
import com.sports.server.game.exception.GameErrorMessages;
import java.util.Arrays;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum GameState {
    PLAYING, FINISHED, SCHEDULED;

    public static GameState from(final String value) {
        return Arrays.stream(GameState.values())
                .filter(state -> state.name().equalsIgnoreCase(value))
                .findFirst().orElseThrow(
                        () -> new CustomException(HttpStatus.BAD_REQUEST, GameErrorMessages.STATE_NOT_FOUND_EXCEPTION));
    }

}
