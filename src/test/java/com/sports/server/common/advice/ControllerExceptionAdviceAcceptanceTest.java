package com.sports.server.common.advice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.sports.server.common.dto.ErrorResponse;
import com.sports.server.support.AcceptanceTest;
import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = {
        "spring.mvc.throw-exception-if-no-handler-found=true",
        "spring.web.resources.add-mappings=false"
})
class ControllerExceptionAdviceAcceptanceTest extends AcceptanceTest {

    @Test
    void 존재하지_않는_엔드포인트_요청시_NoHandlerFoundException_핸들러로_404를_반환한다() {
        // when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .get("/not-exists")
                .then().log().all()
                .extract();

        // then
        ErrorResponse actual = toResponse(response, ErrorResponse.class);
        assertAll(
                () -> assertThat(response.statusCode()).isEqualTo(HttpStatus.NOT_FOUND.value()),
                () -> assertThat(actual.getMessage()).isEqualTo("요청한 엔드포인트를 찾을 수 없습니다.")
        );
    }
}
