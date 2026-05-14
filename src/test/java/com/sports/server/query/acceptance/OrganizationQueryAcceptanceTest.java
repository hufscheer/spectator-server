package com.sports.server.query.acceptance;

import com.sports.server.command.league.domain.League;
import com.sports.server.command.league.domain.LeagueRepository;
import com.sports.server.command.league.domain.Round;
import com.sports.server.command.league.domain.SportType;
import com.sports.server.command.member.domain.Member;
import com.sports.server.command.member.domain.MemberRepository;
import com.sports.server.command.organization.domain.Organization;
import com.sports.server.query.dto.response.OrganizationResponse;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@Sql("/member-fixture.sql")
public class OrganizationQueryAcceptanceTest extends AcceptanceTest {

    @Autowired
    private LeagueRepository leagueRepository;

    @Autowired
    private MemberRepository memberRepository;

    @BeforeEach
    @Override
    protected void setUp() {
        super.setUp();
        Member admin = memberRepository.findMemberByEmailWithOrganization("john@example.com").orElseThrow();
        Organization ongoingOrg = admin.getOrganization();

        LocalDateTime now = LocalDateTime.now();
        leagueRepository.save(new League(
                admin, ongoingOrg, "진행중 리그",
                now.minusDays(1), now.plusDays(1),
                Round.from(8), SportType.SOCCER
        ));
        leagueRepository.save(new League(
                admin, ongoingOrg, "종료된 리그",
                now.minusDays(10), now.minusDays(5),
                Round.from(8), SportType.SOCCER
        ));
    }

    @Test
    void 진행_중인_리그가_있는_학교만_isLeagueOngoing이_true이다() {
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when().get("/organizations")
                .then().log().all()
                .extract();

        List<OrganizationResponse> responses = toResponses(response, OrganizationResponse.class);
        Map<Long, OrganizationResponse> byId = responses.stream()
                .collect(Collectors.toMap(OrganizationResponse::id, r -> r));

        assertAll(
                () -> assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value()),
                () -> assertThat(byId.get(1L).isLeagueOngoing()).isTrue(),
                () -> assertThat(byId.get(2L).isLeagueOngoing()).isFalse(),
                () -> assertThat(byId.get(3L).isLeagueOngoing()).isFalse()
        );
    }
}
