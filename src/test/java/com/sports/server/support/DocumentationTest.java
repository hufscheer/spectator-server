package com.sports.server.support;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.modifyUris;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sports.server.command.cheertalk.application.CheerTalkService;
import com.sports.server.command.cheertalk.presentation.CheerTalkController;
import com.sports.server.command.game.application.GameTeamService;
import com.sports.server.command.game.presentation.GameController;
import com.sports.server.command.report.application.ReportService;
import com.sports.server.command.report.presentation.ReportController;
import com.sports.server.common.log.TimeLogTemplate;
import com.sports.server.query.application.CheerTalkQueryService;
import com.sports.server.query.application.GameQueryService;
import com.sports.server.query.application.GameTeamQueryService;
import com.sports.server.query.application.LeagueQueryService;
import com.sports.server.query.application.LineupPlayerQueryService;
import com.sports.server.query.presentation.CheerTalkQueryController;
import com.sports.server.query.presentation.GameQueryController;
import com.sports.server.query.presentation.LeagueQueryController;
import com.sports.server.support.config.RestDocsConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler;
import org.springframework.restdocs.operation.preprocess.OperationRequestPreprocessor;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = {
        CheerTalkController.class,
        GameController.class,
        ReportController.class,
        CheerTalkQueryController.class,
        GameQueryController.class,
        LeagueQueryController.class,
//        TimelineQueryController.class
})
@Import({
        TimeLogTemplate.class,
        RestDocsConfig.class
})
@AutoConfigureRestDocs
public class DocumentationTest {

    private static final OperationRequestPreprocessor HOST_INFO = preprocessRequest(modifyUris()
            .scheme("https")
            .host("www.api.hufstreaming.site")
            .removePort(), prettyPrint()
    );

    protected static final RestDocumentationResultHandler RESULT_HANDLER = document(
            "{class-name}/{method-name}",
            HOST_INFO,
            preprocessResponse(prettyPrint())
    );

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
}
