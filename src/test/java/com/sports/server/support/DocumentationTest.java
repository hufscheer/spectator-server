package com.sports.server.support;

import tools.jackson.databind.ObjectMapper;
import com.sports.server.auth.application.AuthService;
import com.sports.server.auth.presentation.AuthController;
import com.sports.server.auth.resolver.AuthMemberResolver;
import com.sports.server.auth.utils.JwtUtil;
import com.sports.server.command.bracket.application.BracketService;
import com.sports.server.command.bracket.presentation.BracketController;
import com.sports.server.command.cheertalk.application.CheerTalkService;
import com.sports.server.command.cheertalk.presentation.CheerTalkController;
import com.sports.server.command.game.application.GameService;
import com.sports.server.command.game.application.GameStatusScheduler;
import com.sports.server.command.game.application.GameTeamService;
import com.sports.server.command.game.application.LineupPlayerService;
import com.sports.server.command.game.application.CheerCountRateLimiter;
import com.sports.server.command.game.presentation.GameController;
import com.sports.server.command.league.application.LeagueService;
import com.sports.server.command.league.presentation.LeagueController;
import com.sports.server.command.nl.application.NlFileExtractService;
import com.sports.server.command.nl.application.NlService;
import com.sports.server.command.nl.presentation.NlController;
import com.sports.server.command.member.domain.Member;
import com.sports.server.command.member.domain.MemberRepository;
import com.sports.server.command.player.application.PlayerService;
import com.sports.server.command.player.presentation.PlayerController;
import com.sports.server.command.push.application.PushDeviceService;
import com.sports.server.command.push.presentation.PushDeviceController;
import com.sports.server.command.report.application.ReportService;
import com.sports.server.command.report.presentation.ReportController;
import com.sports.server.command.team.application.TeamService;
import com.sports.server.command.team.presentation.TeamController;
import com.sports.server.command.timeline.application.TimelineService;
import com.sports.server.command.timeline.presentation.TimelineController;
import com.sports.server.common.application.S3Service;
import com.sports.server.common.log.TimeLogTemplate;
import com.sports.server.common.presentation.AppVersionController;
import com.sports.server.common.presentation.S3Controller;
import com.sports.server.query.application.*;
import com.sports.server.query.presentation.*;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.restdocs.test.autoconfigure.AutoConfigureRestDocs;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(
        controllers = {
                CheerTalkController.class,
                GameController.class,
                ReportController.class,
                CheerTalkQueryController.class,
                GameQueryController.class,
                LeagueQueryController.class,
                TeamController.class,
                TeamQueryController.class,
                TeamManagerQueryController.class,
                TimelineQueryController.class,
                AuthController.class,
                LeagueController.class,
                TimelineController.class,
                MemberQueryController.class,
                PlayerController.class,
                PlayerQueryController.class,
                NlController.class,
                OrganizationQueryController.class,
                S3Controller.class,
                BracketController.class,
                PushDeviceController.class,
                AppVersionController.class,
        })
@Import({
        TimeLogTemplate.class,
        RestDocsConfig.class,
        io.micrometer.core.instrument.simple.SimpleMeterRegistry.class,
        com.sports.server.query.support.StudentNumberDisclosurePolicy.class,
})
@AutoConfigureRestDocs
@AutoConfigureMockMvc(addFilters = false)
public class DocumentationTest {

    @Value("${cookie.name}")
    protected String COOKIE_NAME;

    @Autowired
    protected RestDocumentationResultHandler restDocsHandler;

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @MockitoBean
    protected CheerTalkService cheerTalkService;

    @MockitoBean
    protected GameQueryService gameQueryService;

    @MockitoBean
    protected GameTeamQueryService gameTeamQueryService;

    @MockitoBean
    protected LineupPlayerQueryService lineupPlayerQueryService;

    @MockitoBean
    protected GameTeamService gameTeamService;

    @MockitoBean
    protected CheerCountRateLimiter cheerCountRateLimiter;

    @MockitoBean
    protected ReportService reportService;

    @MockitoBean
    protected CheerTalkQueryService cheerTalkQueryService;

    @MockitoBean
    protected LeagueQueryService leagueQueryService;

    @MockitoBean
    protected BracketService bracketService;

    @MockitoBean
    protected BracketQueryService bracketQueryService;

    @MockitoBean
    protected TimelineQueryService timelineQueryService;

    @MockitoBean
    protected LineupPlayerService lineupPlayerService;

    @MockitoBean
    protected AuthService authService;

    @MockitoBean
    protected PlayerService playerService;

    @MockitoBean
    protected PlayerQueryService playerQueryService;

    @MockitoBean
    protected TeamService teamService;

    @MockitoBean
    protected TeamQueryService teamQueryService;

    @MockitoBean
    protected JwtUtil jwtUtil;

    @MockitoBean
    protected AuthenticationEntryPoint authenticationEntryPoint;

    @MockitoBean
    protected MemberRepository memberRepository;

    @MockitoBean
    private AuthMemberResolver authMemberResolver;

    @MockitoBean
    protected TimelineService timelineService;

    @MockitoBean
    protected LeagueService leagueService;

    @MockitoBean
    protected MemberQueryService memberQueryService;

    @MockitoBean
    protected GameService gameService;

    @MockitoBean
    protected GameStatusScheduler gameStatusScheduler;

    @MockitoBean
    protected NlService nlService;

    @MockitoBean
    protected NlFileExtractService nlFileExtractService;

    @MockitoBean
    protected OrganizationQueryService organizationQueryService;

    @MockitoBean
    protected S3Service s3Service;

    @MockitoBean
    protected PushDeviceService pushDeviceService;

    @MockitoBean
    protected com.sports.server.common.application.AlertService alertService;

    @MockitoBean
    protected com.sports.server.common.application.OrganizationAccessValidator organizationAccessValidator;

    @BeforeEach
    void setUp() {
        setupMockAuthentication();
    }

    protected void setupMockAuthentication() {
        String mockEmail = "test@gmail.com";
        Member mockManager = Member.manager(mockEmail, "password");

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(mockEmail, null, List.of())
        );
        Mockito.when(memberRepository.findMemberByEmail(mockEmail))
                .thenReturn(Optional.of(mockManager));
        Mockito.when(memberRepository.findMemberByEmailWithOrganization(mockEmail))
                .thenReturn(Optional.of(mockManager));
        Mockito.when(authMemberResolver.resolveArgument(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(mockManager);
    }
}
