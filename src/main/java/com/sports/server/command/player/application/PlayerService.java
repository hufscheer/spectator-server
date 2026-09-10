package com.sports.server.command.player.application;

import com.sports.server.command.game.domain.LineupPlayerRepository;
import com.sports.server.command.member.domain.Member;
import com.sports.server.command.organization.domain.Organization;
import com.sports.server.command.player.domain.Player;
import com.sports.server.command.player.domain.PlayerRepository;
import com.sports.server.command.player.dto.PlayerConflictResponse;
import com.sports.server.command.player.dto.PlayerRequest;
import com.sports.server.command.player.exception.PlayerErrorMessages;
import com.sports.server.command.player.exception.PlayerStudentNumberConflictException;
import com.sports.server.command.team.domain.TeamPlayer;
import com.sports.server.command.team.domain.TeamPlayerRepository;
import com.sports.server.common.application.EntityUtils;
import com.sports.server.common.application.PermissionValidator;
import com.sports.server.common.exception.BadRequestException;
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
    private final LineupPlayerRepository lineupPlayerRepository;
    private final EntityUtils entityUtils;

    public Long register(final Member member, final PlayerRequest.Register request) {
        Organization organization = member.getOrganization();
        validateUniqueStudentNumber(request.studentNumber(), organization.getId());
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
            validateUniqueStudentNumber(newStudentNumber, member.getOrganization().getId());
        }

        player.update(request.name(), request.studentNumber(), member.getOrganization().getStudentNumberDigits());
    }

    /**
     * 선수는 하드 딜리트다. {@code team_players}·{@code league_top_scorers} 는 연관에 걸려 같이
     * 지워지지만 {@code lineup_players} 는 {@link Player} 에 매핑이 없어 남는다 — 그대로 지우면
     * FK 제약에 걸려 500 이 난다. 실제로 운영 선수 2137명 중 1930명이 이 상태다.
     *
     * <p>라인업을 같이 지우는 선택지는 두지 않았다. 타임라인의 어시스트가
     * {@code timelines.assist_lineup_player_id} 로 라인업을 가리키고 있어, 끝난 경기의 기록이
     * 조용히 사라진다. 소프트 삭제도 마찬가지로 곤란하다 — 팀·대회에서 이미 겪었듯
     * 지워진 행을 가리키는 연관이 LAZY 초기화 때 터진다.
     */
    public void delete(final Member member, final Long playerId) {
        Player player = entityUtils.getEntity(playerId, Player.class);
        PermissionValidator.checkPermission(player, member);
        if (lineupPlayerRepository.existsByPlayer(player)) {
            throw new BadRequestException(PlayerErrorMessages.CANNOT_DELETE_WITH_LINEUPS);
        }
        playerRepository.delete(player);
    }

    private void validateUniqueStudentNumber(String studentNumber, Long organizationId) {
        if (studentNumber == null) {
            return;
        }
        playerRepository.findByStudentNumberAndOrganizationId(studentNumber, organizationId)
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
