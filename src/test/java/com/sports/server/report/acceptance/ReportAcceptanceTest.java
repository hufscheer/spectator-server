package com.sports.server.report.acceptance;

import com.sports.server.report.dto.request.ReportRequest;
import com.sports.server.support.AcceptanceTest;
import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;

import static org.assertj.core.api.Assertions.assertThat;

@Sql(scripts = "/report-fixture.sql")
class ReportAcceptanceTest extends AcceptanceTest {

    @DisplayName("신고를 할 때")
    @Nested
    class ReportTest {
        @Test
        void 존재하는_댓글이면_신고한다() {
            // given
            Long existComment = 1L;
            ReportRequest request = new ReportRequest(existComment);

            // when
            ExtractableResponse<Response> response = 댓글을_신고한다(request);

            // then
            assertThat(response.statusCode()).isEqualTo(HttpStatus.NO_CONTENT.value());
        }

        @Test
        void 존재하지_않는_댓글이면_404를_응답한다() {
            // given
            Long notExistComment = 100L;
            ReportRequest request = new ReportRequest(notExistComment);

            // when
            ExtractableResponse<Response> response = 댓글을_신고한다(request);

            // then
            assertThat(response.statusCode()).isEqualTo(HttpStatus.NOT_FOUND.value());
        }

        @Test
        void 이미_블락된_댓글이면_400을_응답한다() {
            // given
            Long blockedComment = 2L;
            ReportRequest request = new ReportRequest(blockedComment);

            // when
            ExtractableResponse<Response> response = 댓글을_신고한다(request);

            // then
            assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        }

        @Test
        void 이미_신고된_댓글을_또_신고_가능하다() {
            // given
            Long existComment = 1L;
            ReportRequest request = new ReportRequest(existComment);
            댓글을_신고한다(request);

            // when
            ExtractableResponse<Response> response = 댓글을_신고한다(request);

            // then
            assertThat(response.statusCode()).isEqualTo(HttpStatus.NO_CONTENT.value());
        }
    }

    private static ExtractableResponse<Response> 댓글을_신고한다(ReportRequest request) {
        return RestAssured.given().log().all()
                .when()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(request)
                .post("/reports")
                .then().log().all()
                .extract();
    }
}
