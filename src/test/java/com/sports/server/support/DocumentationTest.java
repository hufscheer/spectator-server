package com.sports.server.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sports.server.auth.application.AuthService;
import com.sports.server.auth.filter.JwtAuthorizationFilter;
import com.sports.server.command.cheertalk.application.CheerTalkService;
import com.sports.server.command.game.application.GameTeamService;
import com.sports.server.command.game.application.LineupPlayerService;
import com.sports.server.command.leagueteam.application.LeagueTeamService;
import com.sports.server.command.leagueteam.presentation.LeagueTeamController;
import com.sports.server.command.report.application.ReportService;
import com.sports.server.common.log.TimeLogTemplate;
import com.sports.server.query.application.CheerTalkQueryService;
import com.sports.server.query.application.GameQueryService;
import com.sports.server.query.application.GameTeamQueryService;
import com.sports.server.query.application.LeagueQueryService;
import com.sports.server.query.application.LineupPlayerQueryService;
import com.sports.server.query.application.SportQueryService;
import com.sports.server.query.application.timeline.TimelineQueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
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
    protected SportQueryService sportQueryService;

    @MockBean
    protected LineupPlayerService lineupPlayerService;

    @MockBean
    protected AuthService authService;

    @MockBean
    protected LeagueTeamService leagueTeamService;
}
