package com.sports.server.support;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;

import com.sports.server.command.report.infrastructure.ReportCheckClient;
import com.sports.server.support.config.AsyncTestConfig;
import com.sports.server.support.config.RestDocsConfig;
import com.sports.server.support.isolation.DatabaseIsolation;
import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.ResponseEntity;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

@DatabaseIsolation
@AutoConfigureMockMvc
@ExtendWith(RestDocumentationExtension.class)
@Import({AsyncTestConfig.class, RestDocsConfig.class})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AcceptanceTest {

    @Autowired
    protected RestDocumentationResultHandler restDocsHandler;

    @Autowired
    protected MockMvc mockMvc;

    @LocalServerPort
    protected int port;

    @MockBean
    protected ReportCheckClient reportCheckClient;

    @BeforeEach
    void setUp(
            final WebApplicationContext context,
            final RestDocumentationContextProvider restDocumentation
    ) {
        RestAssured.port = port;

        given(reportCheckClient.check(any()))
                .willReturn(ResponseEntity.ok().build());

        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(documentationConfiguration(restDocumentation))
                .alwaysDo(MockMvcResultHandlers.print())
                .alwaysDo(restDocsHandler)
                .addFilters(new CharacterEncodingFilter("UTF-8", true))
                .build();
    }

    protected <T> List<T> toResponses(ExtractableResponse<Response> response,
                                      Class<T> dtoType) {
        return response.jsonPath()
                .getList(".", dtoType);
    }

    protected <T> T toResponse(ExtractableResponse<Response> response,
                               Class<T> dtoType) {
        return response.jsonPath()
                .getObject(".", dtoType);
    }
}
