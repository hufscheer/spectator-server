package com.sports.server.command.player.application;

import com.sports.server.command.player.domain.Player;
import com.sports.server.command.player.domain.PlayerRepository;
import com.sports.server.command.player.dto.PlayerRequest;
import com.sports.server.common.application.EntityUtils;
import com.sports.server.common.exception.BadRequestException;
import com.sports.server.common.exception.ExceptionMessages;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@Transactional
@RequiredArgsConstructor
public class PlayerService {

    private final PlayerRepository playerRepository;
    private final EntityUtils entityUtils;

    public Long register(final PlayerRequest.Register request){
        validateUniqueStudentNumber(request.studentNumber());

        Player player = request.toEntity(request.name(), request.studentNumber());
        playerRepository.save(player);
        return player.getId();
    }

    public void update(final Long playerId, final PlayerRequest.Update request){
        Player player = entityUtils.getEntity(playerId, Player.class);

        String newStudentNumber = request.studentNumber();
        if (newStudentNumber != null && !newStudentNumber.equals(player.getStudentNumber())) {
            validateUniqueStudentNumber(newStudentNumber);
        }

        player.update(request.name(), request.studentNumber());
    }

    public void delete(final Long playerId){
        Player player = entityUtils.getEntity(playerId, Player.class);
        playerRepository.delete(player);
    }

    private void validateUniqueStudentNumber(String studentNumber) {
        if (studentNumber != null && playerRepository.existsByStudentNumber(studentNumber)) {
            throw new BadRequestException(ExceptionMessages.PLAYER_STUDENT_NUMBER_DUPLICATE);
        }
    }
}
