package com.sports.server.command.league.application;

import com.sports.server.auth.exception.AuthorizationErrorMessages;
import com.sports.server.command.league.domain.League;
import com.sports.server.command.league.domain.LeagueRepository;
import com.sports.server.command.league.dto.LeagueRequestDto;
import com.sports.server.command.member.domain.Member;
import com.sports.server.command.organization.domain.Organization;
import com.sports.server.common.application.EntityUtils;
import com.sports.server.common.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class LeagueService {
    private final EntityUtils entityUtils;
    private final LeagueRepository leagueRepository;

    public void register(final Member manager, final LeagueRequestDto.Register request) {
        Organization organization = entityUtils.getEntity(request.organizationId(), Organization.class);
        leagueRepository.save(request.toEntity(manager, organization));
    }

    public void delete(final Member manager, final Long leagueId) {
        League league = entityUtils.getEntity(leagueId, League.class);
        if (!league.isManagedBy(manager)) {
            throw new UnauthorizedException(AuthorizationErrorMessages.PERMISSION_DENIED);
        }
        league.delete();
    }
}
