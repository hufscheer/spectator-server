package com.sports.server.support;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;

import com.sports.server.auth.utils.JwtUtil;
import com.sports.server.command.report.infrastructure.ReportCheckClient;
import com.sports.server.support.config.AsyncTestConfig;
import com.sports.server.support.isolation.DatabaseIsolation;
import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.ResponseEntity;

@DatabaseIsolation
@Import(AsyncTestConfig.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AcceptanceTest {

    @LocalServerPort
    protected int port;

    @MockBean
    protected ReportCheckClient reportCheckClient;

    @Value("${cookie.name}")
    protected String COOKIE_NAME;

    @MockBean
    protected JwtUtil jwtUtil;

    protected String mockToken = "mockToken";

    @BeforeEach
    void setUp(
    ) {
        RestAssured.port = port;

        given(reportCheckClient.check(any()))
                .willReturn(ResponseEntity.ok().build());
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

    protected void configureMockJwtForEmail(String email) {
        willDoNothing().given(jwtUtil).validateToken(mockToken);
        given(jwtUtil.getEmail(mockToken)).willReturn(email);
    }
}