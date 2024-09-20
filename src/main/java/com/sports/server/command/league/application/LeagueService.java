package com.sports.server.command.league.application;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sports.server.auth.exception.AuthorizationErrorMessages;
import com.sports.server.command.league.domain.League;
import com.sports.server.command.league.domain.LeagueRepository;
import com.sports.server.command.league.domain.Round;
import com.sports.server.command.league.dto.LeagueRequestDto;
import com.sports.server.command.member.domain.Member;
import com.sports.server.command.organization.domain.Organization;
import com.sports.server.common.application.EntityUtils;
import com.sports.server.common.exception.CustomException;
import com.sports.server.common.exception.UnauthorizedException;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class LeagueService {
    private final EntityUtils entityUtils;
    private final LeagueRepository leagueRepository;

	public void register(final Member manager, final LeagueRequestDto.Register request) {
		leagueRepository.save(request.toEntity(manager));
	}

	public void update(final Member manager, final LeagueRequestDto.Update request, final Long leagueId) {
		League league = entityUtils.getEntity(leagueId, League.class);
		if (!league.isManagedBy(manager)) {
			throw new CustomException(HttpStatus.FORBIDDEN, "해당 대회를 수정할 권한이 없습니다.");
		}
		league.updateInfo(request.name(), request.startAt(), request.endAt(), Round.from(request.maxRound()));
	}

    public void delete(final Member manager, final Long leagueId) {
        League league = entityUtils.getEntity(leagueId, League.class);
        if (!league.isManagedBy(manager)) {
            throw new UnauthorizedException(AuthorizationErrorMessages.PERMISSION_DENIED);
        }
        leagueRepository.delete(league);
    }
}
