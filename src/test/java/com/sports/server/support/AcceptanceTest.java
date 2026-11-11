package com.sports.server.support;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;

import com.sports.server.auth.utils.JwtUtil;
import com.sports.server.command.report.application.ReportProcessor;
import com.sports.server.command.report.infrastructure.ReportCheckClient;
import com.sports.server.common.application.S3Service;
import com.sports.server.support.config.AsyncTestConfig;
import com.sports.server.support.isolation.DatabaseIsolation;
import io.restassured.RestAssured;
import io.restassured.config.ObjectMapperConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.mapper.ObjectMapperType;
import io.restassured.path.json.config.JsonParserType;
import io.restassured.path.json.config.JsonPathConfig;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@DatabaseIsolation
@Import(AsyncTestConfig.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AcceptanceTest {

    protected static final String MOCK_EMAIL = "john.doe@example.com";

    @LocalServerPort
    protected int port;

    @MockitoBean
    protected ReportCheckClient reportCheckClient;

    @MockitoBean
    protected ReportProcessor reportProcessor;

    @Value("${cookie.name}")
    protected String COOKIE_NAME;

    @MockitoBean
    protected JwtUtil jwtUtil;

    @MockitoBean
    protected S3Service s3Service;

    protected String mockToken = "mockToken";

    private static final byte[] STUB_PNG = createStubPng();

    @BeforeEach
    protected void setUp(
    ) {
        RestAssured.port = port;
        RestAssured.config = RestAssuredConfig.config()
                .objectMapperConfig(ObjectMapperConfig.objectMapperConfig().defaultObjectMapperType(ObjectMapperType.JACKSON_3));

        given(reportCheckClient.check(any()))
                .willReturn(ResponseEntity.ok().build());

        willDoNothing().given(s3Service).doesFileExist(any());
        given(s3Service.download(any())).willReturn(STUB_PNG);
        willDoNothing().given(s3Service).upload(any(), any(), any());
    }

    private static byte[] createStubPng() {
        try {
            BufferedImage image = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ImageIO.write(image, "png", out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("test stub PNG 생성 실패", e);
        }
    }

    protected <T> List<T> toResponses(ExtractableResponse<Response> response,
                                      Class<T> dtoType) {
        return response.jsonPath()
                .using(JACKSON_3_JSON_PATH)
                .getList(".", dtoType);
    }

    private static final JsonPathConfig JACKSON_3_JSON_PATH =
            JsonPathConfig.jsonPathConfig().defaultParserType(JsonParserType.JACKSON_3);

    protected <T> List<T> toCursorPageContent(ExtractableResponse<Response> response,
                                               Class<T> dtoType) {
        return response.jsonPath()
                .using(JACKSON_3_JSON_PATH)
                .getList("content", dtoType);
    }

    protected <T> T toResponse(ExtractableResponse<Response> response,
                               Class<T> dtoType) {
        return response.jsonPath()
                .using(JACKSON_3_JSON_PATH)
                .getObject(".", dtoType);
    }

    protected void configureMockJwtForEmail(String email) {
        willDoNothing().given(jwtUtil).validateToken(mockToken);
        given(jwtUtil.getEmail(mockToken)).willReturn(email);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(email, null, List.of())
        );
    }
}
