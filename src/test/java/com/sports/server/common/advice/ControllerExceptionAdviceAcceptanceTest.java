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

    @Test
    void 경로_변수_타입이_잘못된_요청시_MethodArgumentTypeMismatchException_핸들러로_400을_반환한다() {
        // when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .get("/games/NaN")
                .then().log().all()
                .extract();

        // then
        ErrorResponse actual = toResponse(response, ErrorResponse.class);
        assertAll(
                () -> assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value()),
                () -> assertThat(actual.getMessage()).isEqualTo("gameId 파라미터의 형식이 올바르지 않습니다.")
        );
    }

    @Test
    void 본문_JSON_파싱에_실패한_요청시_HttpMessageNotReadableException_핸들러로_400을_반환한다() {
        // when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body("{invalid-json")
                .post("/cheer-talks")
                .then().log().all()
                .extract();

        // then
        ErrorResponse actual = toResponse(response, ErrorResponse.class);
        assertAll(
                () -> assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value()),
                () -> assertThat(actual.getMessage()).isEqualTo("요청 본문을 읽을 수 없습니다.")
        );
    }

    @Test
    void 지원하지_않는_ContentType_요청시_HttpMediaTypeNotSupportedException_핸들러로_415를_반환한다() {
        // when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .contentType(MediaType.TEXT_PLAIN_VALUE)
                .body("hello")
                .post("/cheer-talks")
                .then().log().all()
                .extract();

        // then
        ErrorResponse actual = toResponse(response, ErrorResponse.class);
        assertAll(
                () -> assertThat(response.statusCode()).isEqualTo(HttpStatus.UNSUPPORTED_MEDIA_TYPE.value()),
                () -> assertThat(actual.getMessage()).isEqualTo("지원하지 않는 Content-Type입니다.")
        );
    }

    @Test
    void 만족시킬_수_없는_Accept_헤더_요청시_HttpMediaTypeNotAcceptableException_핸들러로_본문_없는_406을_반환한다() {
        // when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .accept(MediaType.APPLICATION_XML_VALUE)
                .get("/organizations")
                .then().log().all()
                .extract();

        // then
        assertAll(
                () -> assertThat(response.statusCode()).isEqualTo(HttpStatus.NOT_ACCEPTABLE.value()),
                () -> assertThat(response.body().asString()).isEmpty()
        );
    }
}
