package com.sports.server.command.cheertalk.application;

import static com.sports.server.command.cheertalk.exception.CheerTalkErrorMessages.CHEER_TALK_CONTAINS_BAD_WORD;

import com.sports.server.auth.exception.AuthorizationErrorMessages;
import com.sports.server.command.cheertalk.domain.CheerTalk;
import com.sports.server.command.cheertalk.domain.CheerTalkRepository;
import com.sports.server.command.cheertalk.domain.LanguageFilter;
import com.sports.server.command.cheertalk.dto.CheerTalkRequest;
import com.sports.server.command.cheertalk.exception.CheerTalkErrorMessages;
import com.sports.server.command.league.domain.League;
import com.sports.server.command.member.domain.Member;
import com.sports.server.command.report.domain.Report;
import com.sports.server.command.report.domain.ReportRepository;
import com.sports.server.command.report.domain.ReportState;
import com.sports.server.command.report.exception.ReportErrorMessage;
import com.sports.server.common.application.EntityUtils;
import com.sports.server.common.exception.CustomException;
import com.sports.server.common.exception.UnauthorizedException;
import com.sports.server.query.repository.CheerTalkDynamicRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class CheerTalkService {

    private final CheerTalkRepository cheerTalkRepository;
    private final ReportRepository reportRepository;
    private final LanguageFilter languageFilter;
    private final EntityUtils entityUtils;

    public void register(final CheerTalkRequest cheerTalkRequest) {
        validateContent(cheerTalkRequest.content());
        CheerTalk cheerTalk = new CheerTalk(cheerTalkRequest.content(), cheerTalkRequest.gameTeamId());
        cheerTalkRepository.save(cheerTalk);
    }

    private void validateContent(final String content) {
        if (languageFilter.containsBadWord(content)) {
            throw new CustomException(HttpStatus.BAD_REQUEST, CHEER_TALK_CONTAINS_BAD_WORD);
        }
    }

    public void block(final Long leagueId, final Long cheerTalkId, final Member manager) {
        checkPermission(leagueId, manager);

        CheerTalk cheerTalk = entityUtils.getEntity(cheerTalkId, CheerTalk.class);

        Optional<Report> report = reportRepository.findByCheerTalk(cheerTalk);
        report.ifPresent(Report::updateToValid);
        cheerTalk.block();
    }

    public void unblock(final Long leagueId, final Long cheerTalkId, final Member manager) {
        checkPermission(leagueId, manager);

        CheerTalk cheerTalk = entityUtils.getEntity(cheerTalkId, CheerTalk.class);

        Optional<Report> report = reportRepository.findByCheerTalk(cheerTalk);
        report.ifPresent(Report::updateToInvalid);
        cheerTalk.unblock();
    }

    private void checkPermission(final Long leagueId, final Member manager) {

        League league = entityUtils.getEntity(leagueId, League.class);

        if (!league.isManagedBy(manager)) {
            throw new UnauthorizedException(AuthorizationErrorMessages.PERMISSION_DENIED);
        }
    }
}
