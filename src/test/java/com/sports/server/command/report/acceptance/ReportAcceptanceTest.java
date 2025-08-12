package com.sports.server.command.report.acceptance;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

import com.sports.server.command.report.dto.ReportRequest;
import com.sports.server.query.dto.response.CheerTalkResponse;
import com.sports.server.support.AcceptanceTest;
import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;

@ActiveProfiles("test")
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
            assertAll(
                    () -> assertThat(response.statusCode()).isEqualTo(HttpStatus.NO_CONTENT.value()),
                    () -> verify(reportProcessor).check(any(), any())
            );
        }

        @Test
        void 존재하지_않는_댓글이면_404를_응답한다() {
            // given
            Long notExistComment = 100L;
            ReportRequest request = new ReportRequest(notExistComment);

            // when
            ExtractableResponse<Response> response = 댓글을_신고한다(request);

            // then
            assertAll(
                    () -> assertThat(response.statusCode()).isEqualTo(HttpStatus.NOT_FOUND.value()),
                    () -> verify(reportProcessor, never()).check(any(), any())
            );
        }

        @Test
        void 이미_블락된_댓글이면_400을_응답한다() {
            // given
            Long blockedComment = 2L;
            ReportRequest request = new ReportRequest(blockedComment);

            // when
            ExtractableResponse<Response> response = 댓글을_신고한다(request);

            // then
            assertAll(
                    () -> assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value()),
                    () -> verify(reportProcessor, never()).check(any(), any())
            );
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
            assertAll(
                    () -> assertThat(response.statusCode()).isEqualTo(HttpStatus.NO_CONTENT.value()),
                    () -> verify(reportProcessor, times(2)).check(any(), any())
            );
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

    @Test
    void 신고를_무효처리한다() {
        // given
        Long leagueId = 1L;
        Long cheerTalkId = 4L;

        configureMockJwtForEmail(MOCK_EMAIL);

        // when
        ExtractableResponse<Response> patchResponse = RestAssured.given().log().all()
                .cookie(COOKIE_NAME, mockToken)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .patch("/reports/{leagueId}/{cheerTalkId}/cancel", leagueId, cheerTalkId)
                .then().log().all()
                .extract();

        // then
        ExtractableResponse<Response> getResponse = RestAssured.given().log().all()
                .when()
                .cookie(COOKIE_NAME, mockToken)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .get("/leagues/{leagueId}/cheer-talks/reported", leagueId)
                .then().log().all()
                .extract();

        List<CheerTalkResponse.ForManager> reportedList = toResponses(getResponse, CheerTalkResponse.ForManager.class).stream()
                .filter(reported -> reported.cheerTalkId().equals(cheerTalkId))
                .toList();;
        assertAll(
                () -> assertThat(patchResponse.statusCode()).isEqualTo(HttpStatus.OK.value()),
                () -> assertThat(reportedList.size()).isEqualTo(0)
        );
    }
}
