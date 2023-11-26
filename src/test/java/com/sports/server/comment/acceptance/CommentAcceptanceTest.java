package com.sports.server.comment.acceptance;

import com.sports.server.comment.dto.request.CommentRequestDto;
import com.sports.server.comment.dto.response.CommentResponse;
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

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@Sql(scripts = "/comment-fixture.sql")
public class CommentAcceptanceTest extends AcceptanceTest {

    @Test
    void 새로운_댓글이_저장된다() {

        // given
        String content = "파이팅!";
        Long gameTeamId = 1L;
        CommentRequestDto commentRequestDto = new CommentRequestDto(content, gameTeamId);

        // when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .when()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(commentRequestDto)
                .post("/comments")
                .then().log().all()
                .extract();

        //then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
    }

    @DisplayName("게임의 댓글을 조회할 때")
    @Nested
    class GetCommentsTest {

        @Test
        void 커서와_사이즈가_없으면_최신_10개가_조회된다() {
            // given
            Long gameId = 1L;

            // when
            ExtractableResponse<Response> response = RestAssured.given().log().all()
                    .when()
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .get("/games/{gameId}/comments", gameId)
                    .then().log().all()
                    .extract();

            // then
            List<CommentResponse> actual = toResponses(response, CommentResponse.class);
            assertAll(
                    () -> assertThat(actual).hasSize(10),
                    () -> assertThat(actual.get(9))
                            .isEqualTo(new CommentResponse(
                                    1L,
                                    "댓글1",
                                    1L,
                                    LocalDateTime.of(2023, 1, 1, 12, 30, 0),
                                    false
                            )),
                    () -> assertThat(actual)
                            .map(CommentResponse::commentId)
                            .containsExactly(10L, 9L, 8L, 7L, 6L, 5L, 4L, 3L, 2L, 1L)
            );
        }

        @Test
        void 커서가_있으면_커서_다음부터_10개가_조회된다() {
            // given
            Long gameId = 1L;
            Long cursor = 4L;

            // when
            ExtractableResponse<Response> response = RestAssured.given().log().all()
                    .when()
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .queryParam("cursor", cursor)
                    .get("/games/{gameId}/comments", gameId)
                    .then().log().all()
                    .extract();

            // then
            List<CommentResponse> actual = toResponses(response, CommentResponse.class);
            assertAll(
                    () -> assertThat(actual).hasSize(10),

                    () -> assertThat(actual)
                            .map(CommentResponse::commentId)
                            .containsExactly(14L, 13L, 12L, 11L, 10L, 9L, 8L, 7L, 6L, 5L)
            );
        }

        @Test
        void 사이즈만큼_조회된다() {
            // given
            Long gameId = 1L;
            int size = 5;

            // when
            ExtractableResponse<Response> response = RestAssured.given().log().all()
                    .when()
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .queryParam("size", size)
                    .get("/games/{gameId}/comments", gameId)
                    .then().log().all()
                    .extract();

            // then
            List<CommentResponse> actual = toResponses(response, CommentResponse.class);
            assertAll(
                    () -> assertThat(actual).hasSize(size),

                    () -> assertThat(actual)
                            .map(CommentResponse::commentId)
                            .containsExactly(5L, 4L, 3L, 2L, 1L)
            );
        }

        @Test
        void 커서_다음부터_사이즈만큼_조회된다() {
            // given
            Long gameId = 1L;
            int size = 5;
            Long cursor = 8L;

            // when
            ExtractableResponse<Response> response = RestAssured.given().log().all()
                    .when()
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .queryParam("size", size)
                    .queryParam("cursor", cursor)
                    .get("/games/{gameId}/comments", gameId)
                    .then().log().all()
                    .extract();

            // then
            List<CommentResponse> actual = toResponses(response, CommentResponse.class);
            assertAll(
                    () -> assertThat(actual).hasSize(size),

                    () -> assertThat(actual)
                            .map(CommentResponse::commentId)
                            .containsExactly(13L, 12L, 11L, 10L, 9L)
            );
        }

        @Test
        void 블락된_댓글은_null을_표시한다() {
            // given
            Long gameId = 1L;
            Long cursor = 10L;

            // when
            ExtractableResponse<Response> response = RestAssured.given().log().all()
                    .when()
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .queryParam("cursor", cursor)
                    .get("/games/{gameId}/comments", gameId)
                    .then().log().all()
                    .extract();

            // then
            List<CommentResponse> actual = toResponses(response, CommentResponse.class);
            assertThat(actual)
                    .filteredOn(CommentResponse::isBlocked)
                    .map(CommentResponse::content)
                    .containsOnlyNulls();
        }
    }

}
