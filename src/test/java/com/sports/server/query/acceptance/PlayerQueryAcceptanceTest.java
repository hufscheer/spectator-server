package com.sports.server.query.acceptance;

import com.sports.server.command.member.domain.Member;
import com.sports.server.command.member.domain.MemberRepository;
import com.sports.server.command.organization.domain.Organization;
import com.sports.server.command.player.domain.Player;
import com.sports.server.command.player.domain.PlayerRepository;
import com.sports.server.query.dto.response.PlayerResponse;
import com.sports.server.support.AcceptanceTest;
import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@Sql("/member-fixture.sql")
public class PlayerQueryAcceptanceTest extends AcceptanceTest {

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private MemberRepository memberRepository;

    private Player player1;
    private Player player2;

    @Override
    @BeforeEach
    protected void setUp() {
        super.setUp();
        Member member = memberRepository.findMemberByEmailWithOrganization("john@example.com").orElseThrow();
        Organization organization = member.getOrganization();

        player1 = new Player("손흥민", "202500111", 9);
        player1.setOrganization(organization);
        player2 = new Player("박지성", "202500112", 9);
        player2.setOrganization(organization);
        playerRepository.save(player1);
        playerRepository.save(player2);
    }

    @Test
    void 모든_선수를_조회한다() {
        configureMockJwtForEmail("john@example.com");

        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .when()
                .cookie(COOKIE_NAME, mockToken)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .get("/players")
                .then().log().all()
                .extract();

        // then
        List<PlayerResponse> actual = toResponses(response, PlayerResponse.class);
        List<PlayerResponse> expected = List.of(new PlayerResponse(player2, null), new PlayerResponse(player1, null));

        assertAll(
                () -> assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value()),
                () -> assertThat(actual).usingRecursiveComparison().isEqualTo(expected)
        );
    }

    @Test
    void 선수를_상세_조회한다(){
        // given
        Long playerId = player1.getId();
        PlayerResponse expected = new PlayerResponse(player1, null);

        // when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .when()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .get("/players/{playerId}", playerId)
                .then().log().all()
                .extract();

        // then
        PlayerResponse actual = toResponse(response, PlayerResponse.class);

        assertAll(
                () -> assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value()),
                () -> assertThat(actual).isEqualTo(expected)
        );

    }

}
