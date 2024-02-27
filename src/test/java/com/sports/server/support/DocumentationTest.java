package com.sports.server.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sports.server.command.cheertalk.application.CheerTalkService;
import com.sports.server.command.cheertalk.presentation.CheerTalkController;
import com.sports.server.command.game.application.GameTeamService;
import com.sports.server.command.game.presentation.GameController;
import com.sports.server.command.report.application.ReportService;
import com.sports.server.command.report.presentation.ReportController;
import com.sports.server.common.log.TimeLogTemplate;
import com.sports.server.query.application.*;
import com.sports.server.query.application.timeline.TimelineQueryService;
import com.sports.server.query.presentation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = {
        CheerTalkController.class,
        GameController.class,
        ReportController.class,
        CheerTalkQueryController.class,
        GameQueryController.class,
        LeagueQueryController.class,
        TimelineQueryController.class,
        SportQueryController.class
})
@Import({
        TimeLogTemplate.class,
        RestDocsConfig.class
})
@AutoConfigureRestDocs
public class DocumentationTest {

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
}
