package com.sports.server.common.application;

import com.sports.server.auth.exception.AuthorizationErrorMessages;
import com.sports.server.command.league.domain.LeagueRepository;
import com.sports.server.command.member.domain.Member;
import com.sports.server.command.organization.domain.Organization;
import com.sports.server.command.player.domain.Player;
import com.sports.server.command.team.domain.Team;
import com.sports.server.common.exception.ForbiddenException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 팀과 선수는 학교 단위로 관리한다. 슈퍼유저, 같은 학교 매니저, 그리고 그 학교 대회(삭제되지 않은)의 관리자가 다룰 수 있다.
 * 마지막 조건은 다른 학교 대회를 대신 운영하는 계정 때문이다. 대회의 학교는 만든 매니저의 학교로만 정해지므로
 * 매니저가 스스로 다른 학교 대회의 관리자가 될 수는 없다.
 */
@Component
@RequiredArgsConstructor
public class OrganizationAccessValidator {

    private final LeagueRepository leagueRepository;

    public boolean canManage(final Member member, final Organization organization) {
        if (member.isSuperUser()) {
            return true;
        }
        if (organization == null) {
            return false;
        }
        if (member.getOrganization() != null && organization.getId().equals(member.getOrganization().getId())) {
            return true;
        }
        return leagueRepository.existsByAdministratorAndOrganization(member.getId(), organization.getId());
    }

    public boolean canManage(final Member member, final Team team) {
        return canManage(member, team.getUnit().getOrganization());
    }

    public boolean canManage(final Member member, final Player player) {
        return canManage(member, player.getOrganization());
    }

    public void checkTeam(final Team team, final Member member) {
        if (!canManage(member, team)) {
            throw new ForbiddenException(AuthorizationErrorMessages.PERMISSION_DENIED);
        }
    }

    public void checkPlayer(final Player player, final Member member) {
        if (!canManage(member, player)) {
            throw new ForbiddenException(AuthorizationErrorMessages.PERMISSION_DENIED);
        }
    }
}
