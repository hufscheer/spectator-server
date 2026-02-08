package com.sports.server.command.cheertalk.application;

import static com.sports.server.command.cheertalk.exception.CheerTalkErrorMessages.CHEER_TALK_CONTAINS_BAD_WORD;

import com.sports.server.command.cheertalk.domain.*;
import com.sports.server.command.cheertalk.dto.CheerTalkRequest;
import com.sports.server.command.game.domain.GameTeam;
import com.sports.server.command.game.domain.GameTeamRepository;
import com.sports.server.command.league.domain.League;
import com.sports.server.command.member.domain.Member;
import com.sports.server.command.report.domain.Report;
import com.sports.server.command.report.domain.ReportRepository;
import com.sports.server.common.application.EntityUtils;
import com.sports.server.common.application.PermissionValidator;
import com.sports.server.common.exception.BadRequestException;
import com.sports.server.common.exception.ExceptionMessages;
import com.sports.server.common.exception.NotFoundException;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class CheerTalkService {

    private final CheerTalkRepository cheerTalkRepository;
    private final ReportRepository reportRepository;
    private final GameTeamRepository gameTeamRepository;
    private final EntityUtils entityUtils;
    private final ApplicationEventPublisher eventPublisher;

    public void register(final CheerTalkRequest cheerTalkRequest) {
        GameTeam gameTeam = getGameTeam(cheerTalkRequest.gameTeamId());

        CheerTalk cheerTalk = new CheerTalk(cheerTalkRequest.content(), gameTeam.getId());
        cheerTalkRepository.save(cheerTalk);

        eventPublisher.publishEvent(new CheerTalkCreateEvent(cheerTalk, gameTeam.getGame().getId()));
    }

    public void block(final Long leagueId, final Long cheerTalkId, final Member manager) {
        League league = entityUtils.getEntity(leagueId, League.class);
        PermissionValidator.checkPermission(league, manager);

        blockById(cheerTalkId);
    }

    public void blockById(final Long cheerTalkId) {
        CheerTalk cheerTalk = entityUtils.getEntity(cheerTalkId, CheerTalk.class);

        Optional<Report> report = reportRepository.findByCheerTalk(cheerTalk);
        if (report.isPresent()) {
            report.get().accept();
            return;
        }
        cheerTalk.blockByAdmin();
    }

    public void blockByBot(final Long cheerTalkId) {
        CheerTalk cheerTalk = entityUtils.getEntity(cheerTalkId, CheerTalk.class);

        Optional<Report> report = reportRepository.findByCheerTalk(cheerTalk);
        if (report.isPresent()) {
            report.get().accept();
            return;
        }
        cheerTalk.blockByBot();
    }

    public void unblock(final Long leagueId, final Long cheerTalkId, final Member manager) {
        League league = entityUtils.getEntity(leagueId, League.class);
        PermissionValidator.checkPermission(league, manager);

        CheerTalk cheerTalk = entityUtils.getEntity(cheerTalkId, CheerTalk.class);

        Optional<Report> report = reportRepository.findByCheerTalk(cheerTalk);
        report.ifPresent(Report::cancel);
        cheerTalk.unblock();
    }

    private GameTeam getGameTeam(Long gameTeamId) {
        return gameTeamRepository.findByIdWithGame(gameTeamId)
                .orElseThrow(() -> new NotFoundException(ExceptionMessages.CHEER_TALK_GAME_TEAM_NOT_FOUND));
    }
}
