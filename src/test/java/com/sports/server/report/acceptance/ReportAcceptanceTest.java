package com.sports.server.report.acceptance;

import com.sports.server.report.dto.request.ReportRequest;
import com.sports.server.support.AcceptanceTest;
import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;

import static org.assertj.core.api.Assertions.assertThat;

@Sql(scripts = "/report-fixture.sql")
public class ReportAcceptanceTest extends AcceptanceTest {

    @Test
    void 댓글을_신고한다() {
        // given
        Long commentId = 1L;
        ReportRequest request = new ReportRequest(commentId);

        // when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .when()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(request)
                .post("/reports")
                .then().log().all()
                .extract();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.NO_CONTENT.value());
    }
}
