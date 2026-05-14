package com.sports.server.command.player.application;

import com.sports.server.command.member.domain.Member;
import com.sports.server.command.organization.domain.Organization;
import com.sports.server.command.player.domain.Player;
import com.sports.server.command.player.domain.PlayerRepository;
import com.sports.server.command.player.dto.PlayerConflictResponse;
import com.sports.server.command.player.dto.PlayerRequest;
import com.sports.server.command.player.exception.PlayerStudentNumberConflictException;
import com.sports.server.command.team.domain.TeamPlayer;
import com.sports.server.command.team.domain.TeamPlayerRepository;
import com.sports.server.common.application.EntityUtils;
import com.sports.server.common.application.PermissionValidator;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class PlayerService {

    private final PlayerRepository playerRepository;
    private final TeamPlayerRepository teamPlayerRepository;
    private final EntityUtils entityUtils;

    public Long register(final Member member, final PlayerRequest.Register request) {
        validateUniqueStudentNumber(request.studentNumber());
        Organization organization = member.getOrganization();
        Player player = new Player(request.name(), request.studentNumber(), organization.getStudentNumberDigits());
        player.setOrganization(organization);
        playerRepository.save(player);
        return player.getId();
    }

    public void update(final Member member, final Long playerId, final PlayerRequest.Update request) {
        Player player = entityUtils.getEntity(playerId, Player.class);
        PermissionValidator.checkPermission(player, member);

        String newStudentNumber = request.studentNumber();
        if (newStudentNumber != null && !newStudentNumber.equals(player.getStudentNumber())) {
            validateUniqueStudentNumber(newStudentNumber);
        }

        player.update(request.name(), request.studentNumber(), member.getOrganization().getStudentNumberDigits());
    }

    public void delete(final Member member, final Long playerId) {
        Player player = entityUtils.getEntity(playerId, Player.class);
        PermissionValidator.checkPermission(player, member);
        playerRepository.delete(player);
    }

    private void validateUniqueStudentNumber(String studentNumber) {
        if (studentNumber == null) {
            return;
        }
        playerRepository.findByStudentNumber(studentNumber)
                .ifPresent(existing -> {
                    throw new PlayerStudentNumberConflictException(buildConflictPlayer(existing));
                });
    }

    private PlayerConflictResponse.ConflictPlayer buildConflictPlayer(Player existing) {
        List<PlayerConflictResponse.ConflictTeam> teams = teamPlayerRepository.findAllByPlayerId(existing.getId())
                .stream()
                .map(this::toConflictTeam)
                .toList();
        return new PlayerConflictResponse.ConflictPlayer(
                existing.getId(),
                existing.getName(),
                existing.getStudentNumber(),
                teams
        );
    }

    private PlayerConflictResponse.ConflictTeam toConflictTeam(TeamPlayer teamPlayer) {
        return new PlayerConflictResponse.ConflictTeam(
                teamPlayer.getTeam().getId(),
                teamPlayer.getTeam().getName(),
                teamPlayer.getTeam().getUnit().getName(),
                teamPlayer.getTeam().getSportType().name()
        );
    }
}
