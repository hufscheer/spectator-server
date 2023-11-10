package com.sports.server.support;

import com.sports.server.support.isolation.DatabaseIsolation;
import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.util.List;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DatabaseIsolation
public class AcceptanceTest {

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
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
