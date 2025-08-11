package com.sports.server.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sports.server.auth.application.AuthService;
import com.sports.server.auth.presentation.AuthController;
import com.sports.server.auth.resolver.AuthMemberResolver;
import com.sports.server.auth.utils.JwtUtil;
import com.sports.server.command.cheertalk.application.CheerTalkService;
import com.sports.server.command.cheertalk.presentation.CheerTalkController;
import com.sports.server.command.game.application.GameService;
import com.sports.server.command.game.application.GameTeamService;
import com.sports.server.command.game.application.LineupPlayerService;
import com.sports.server.command.game.presentation.GameController;
import com.sports.server.command.league.application.LeagueService;
import com.sports.server.command.league.presentation.LeagueController;
import com.sports.server.command.member.domain.Member;
import com.sports.server.command.member.domain.MemberRepository;
import com.sports.server.command.player.application.PlayerService;
import com.sports.server.command.player.presentation.PlayerController;
import com.sports.server.command.report.application.ReportService;
import com.sports.server.command.report.presentation.ReportController;
import com.sports.server.command.timeline.application.TimelineService;
import com.sports.server.command.timeline.presentation.TimelineController;
import com.sports.server.common.log.TimeLogTemplate;
import com.sports.server.query.application.*;
import com.sports.server.query.presentation.*;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
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
                TimelineQueryController.class,
                AuthController.class,
                LeagueController.class,
                TimelineController.class,
                MemberQueryController.class,
                PlayerController.class,
                PlayerQueryController.class,
        })
@Import({
        TimeLogTemplate.class,
        RestDocsConfig.class,
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

    @MockBean
    protected CheerTalkService cheerTalkService;

    @MockBean
    protected GameQueryService gameQueryService;

    @MockBean
    protected GameTeamQueryService gameTeamQueryService;

    @MockBean
    protected LineupPlayerQueryService lineupPlayerQueryService;

    @MockBean
    protected GameTeamService gameTeamService;

    @MockBean
    protected ReportService reportService;

    @MockBean
    protected CheerTalkQueryService cheerTalkQueryService;

    @MockBean
    protected LeagueQueryService leagueQueryService;

    @MockBean
    protected TimelineQueryService timelineQueryService;

    @MockBean
    protected LineupPlayerService lineupPlayerService;

    @MockBean
    protected AuthService authService;

    @MockBean
    protected PlayerService playerService;

    @MockBean
    protected PlayerQueryService playerQueryService;

    @MockBean
    protected JwtUtil jwtUtil;

    @MockBean
    protected AuthenticationEntryPoint authenticationEntryPoint;

    @MockBean
    protected MemberRepository memberRepository;

    @MockBean
    private AuthMemberResolver authMemberResolver;

    @MockBean
    protected TimelineService timelineService;

    @MockBean
    protected LeagueService leagueService;

    @MockBean
    protected MemberQueryService memberQueryService;

    @MockBean
    protected GameService gameService;

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
        Mockito.when(authMemberResolver.resolveArgument(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(mockManager);
    }
}
