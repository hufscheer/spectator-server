package com.sports.server.comment.acceptance;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.sports.server.comment.dto.request.CommentRequestDto;
import com.sports.server.comment.dto.response.CommentResponseDto;
import com.sports.server.support.AcceptanceTest;
import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;

@Sql(scripts = "/comment-fixture.sql")
public class CommentAcceptanceTest extends AcceptanceTest {

    @Test
    void 새로운_댓글이_저장된다() {

        // given
        String content = "파이팅!";
        Long gameTeamId = 1L;
        Long gameId = 1L;
        CommentRequestDto commentRequestDto = new CommentRequestDto(content, gameTeamId);

        // when
        RestAssured.given().log().all()
                .when()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(commentRequestDto)
                .post("/comments/register")
                .then().log().all()
                .extract();

        // then
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .when()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .get("/games/{gameId}/comments", gameId)
                .then().log().all()
                .extract();

        //then
        List<CommentResponseDto> actual = toResponses(response, CommentResponseDto.class);

        assertAll(
                () -> Assertions.assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value()),
                () -> assertThat(actual)
                        .map(CommentResponseDto::getContent)
                        .contains(content)
        );
    }

}
