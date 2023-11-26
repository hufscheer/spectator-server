package com.sports.server.support;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import com.sports.server.report.infrastructure.ReportCheckClient;
import com.sports.server.support.config.AsyncTestConfig;
import com.sports.server.support.isolation.DatabaseIsolation;
import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.ResponseEntity;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(AsyncTestConfig.class)
@DatabaseIsolation
public class AcceptanceTest {

    @LocalServerPort
    protected int port;

    @MockBean
    protected ReportCheckClient reportCheckClient;

    @BeforeEach
    void setUp() {
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
}
