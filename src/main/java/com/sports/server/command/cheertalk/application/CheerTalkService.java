package com.sports.server.command.cheertalk.application;

import static com.sports.server.command.cheertalk.exception.CheerTalkErrorMessages.CHEER_TALK_CONTAINS_BAD_WORD;

import com.fasterxml.jackson.databind.JsonNode;
import com.sports.server.command.cheertalk.application.mapper.CheerTalkFilterResponseMapper;
import com.sports.server.command.cheertalk.domain.*;
import com.sports.server.command.cheertalk.dto.CheerTalkFilterResponse;
import com.sports.server.command.cheertalk.dto.CheerTalkRequest;
import com.sports.server.command.cheertalk.infra.CheerTalkBotClient;
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
import java.util.List;
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
    private final LanguageFilter languageFilter;
    private final EntityUtils entityUtils;
    private final ApplicationEventPublisher eventPublisher;
    private final CheerTalkBotClient huggingfaceClient;
    private final List<CheerTalkFilterResponseMapper> mappers;
    private final CheerTalkBotFilterHistoryRepository historyRepository;

    public void register(final CheerTalkRequest cheerTalkRequest) {
        validateContent(cheerTalkRequest.content());
        GameTeam gameTeam = getGameTeam(cheerTalkRequest.gameTeamId());

        CheerTalk cheerTalk = new CheerTalk(cheerTalkRequest.content(), gameTeam.getId());
        cheerTalkRepository.save(cheerTalk);

        eventPublisher.publishEvent(new CheerTalkCreateEvent(cheerTalk, gameTeam.getGame().getId()));
    }

    private void validateContent(final String content) {
        if (languageFilter.containsBadWord(content)) {
            throw new BadRequestException(CHEER_TALK_CONTAINS_BAD_WORD);
        }
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
        cheerTalk.block();
    }

    public void unblock(final Long leagueId, final Long cheerTalkId, final Member manager) {
        League league = entityUtils.getEntity(leagueId, League.class);
        PermissionValidator.checkPermission(league, manager);

        CheerTalk cheerTalk = entityUtils.getEntity(cheerTalkId, CheerTalk.class);

        Optional<Report> report = reportRepository.findByCheerTalk(cheerTalk);
        report.ifPresent(Report::cancel);
        cheerTalk.unblock();
    }

    /**
     * ai 모델 이용한 구체 필터링 로직 구현
     * @return CheerTalkBotFilterResult(CLEAN or ABUSIVE)
     * 필터링 관련 서비스 별도 생성 권장
     */
    public CheerTalkBotFilterResult filterByBot(String content){
        long startTime = System.currentTimeMillis();

        // 1. API 호출
        JsonNode rawResponse = huggingfaceClient.detectAbusiveContent(content);
        int latencyMs = (int) (System.currentTimeMillis() - startTime);

        // 2. BotType에 맞는 Mapper 찾기
        BotType botType = huggingfaceClient.supports();
        CheerTalkFilterResponseMapper mapper = mappers.stream()
                .filter(m -> m.supports() == botType)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No mapper found for BotType: " + botType));

        // 3. Mapper로 응답 파싱
        CheerTalkFilterResponse response = mapper.map(rawResponse, latencyMs);

        // 4. History 저장
        CheerTalkBotFilterHistory history = new CheerTalkBotFilterHistory(
                null,
                response.result(),
                response.botType(),
                response.rawResponse(),
                response.latencyMs()
        );
        historyRepository.save(history);

        // 5. 결과 반환
        return response.result();
    }

    private GameTeam getGameTeam(Long gameTeamId) {
        return gameTeamRepository.findByIdWithGame(gameTeamId)
                .orElseThrow(() -> new NotFoundException(ExceptionMessages.CHEER_TALK_GAME_TEAM_NOT_FOUND));
    }
}
