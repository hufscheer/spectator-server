package com.sports.server.command.player.exception;

import com.sports.server.command.player.dto.PlayerConflictResponse;
import com.sports.server.common.exception.CustomException;
import com.sports.server.common.exception.ExceptionMessages;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class PlayerStudentNumberConflictException extends CustomException {

    private final PlayerConflictResponse.ConflictPlayer existingPlayer;

    public PlayerStudentNumberConflictException(PlayerConflictResponse.ConflictPlayer existingPlayer) {
        super(HttpStatus.CONFLICT, ExceptionMessages.PLAYER_STUDENT_NUMBER_DUPLICATE);
        this.existingPlayer = existingPlayer;
    }
}
