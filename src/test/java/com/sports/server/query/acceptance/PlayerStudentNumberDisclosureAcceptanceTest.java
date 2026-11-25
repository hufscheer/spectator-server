package com.sports.server.query.acceptance;

import static org.assertj.core.api.Assertions.assertThat;

import com.sports.server.query.dto.response.PlayerResponse;
import com.sports.server.query.dto.response.TeamDetailResponse;
import com.sports.server.support.AcceptanceTest;
import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;

/**
 * 공개 GET 4곳이 선수 학번을 관리 권한이 있는 매니저에게만 전부 내보낸다.
 * 학번은 학교마다 자리수가 달라(9, 10자리, 미정) 마스킹해도 substring(2, 4)(입학연도)가 같아야 한다.
 */
@Sql("/player-student-number-fixture.sql")
class PlayerStudentNumberDisclosureAcceptanceTest extends AcceptanceTest {

    private static final Map<Long, String> ORIGINAL = new LinkedHashMap<>();
    private static final Map<Long, String> MASKED = new LinkedHashMap<>();
    private static final Map<Long, String> ADMISSION_YEAR = new LinkedHashMap<>();
    private static final Long OTHER_SCHOOL_PLAYER = 7L;
    private static final List<Long> ALL_IDS = List.of(1L, 2L, 3L, 4L, 5L, 6L, 7L);

    static {
        ORIGINAL.put(1L, "202012345");
        ORIGINAL.put(2L, "2021123456");
        ORIGINAL.put(3L, "331234567");
        ORIGINAL.put(4L, null);
        ORIGINAL.put(5L, "123");
        ORIGINAL.put(6L, "2012");
        ORIGINAL.put(7L, "202298765");

        MASKED.put(1L, "2020*****");
        MASKED.put(2L, "2021******");
        MASKED.put(3L, "3312*****");
        MASKED.put(4L, null);
        MASKED.put(5L, null);
        MASKED.put(6L, null);
        MASKED.put(7L, "2022*****");

        ADMISSION_YEAR.put(1L, "20");
        ADMISSION_YEAR.put(2L, "21");
        ADMISSION_YEAR.put(3L, "12");
        ADMISSION_YEAR.put(4L, null);
        ADMISSION_YEAR.put(5L, null);
        ADMISSION_YEAR.put(6L, null);
        ADMISSION_YEAR.put(7L, "22");
    }

    private record Source(String name, Function<String, Fetched> fetch) {
    }

    private record Item(ExtractableResponse<Response> response, List<Long> playerIds) {
    }

    private record Fetched(Map<Long, PlayerResponse> players, List<Item> items) {
    }

    private List<Source> sources() {
        return List.of(
                new Source("GET /players/{id}", this::fetchPlayerDetails),
                new Source("GET /teams/{id}", email -> {
                    ExtractableResponse<Response> response = get(email, "/teams/{id}", 1L);
                    return new Fetched(byId(toResponse(response, TeamDetailResponse.class).teamPlayers()),
                            List.of(new Item(response, ALL_IDS)));
                }),
                new Source("GET /teams/{id}/players", email -> {
                    ExtractableResponse<Response> response = get(email, "/teams/{id}/players", 1L);
                    return new Fetched(byId(toResponses(response, PlayerResponse.class)), List.of(new Item(response, ALL_IDS)));
                }),
                new Source("GET /leagues/teams/{id}/players", email -> {
                    ExtractableResponse<Response> response = get(email, "/leagues/teams/{id}/players", 1L);
                    return new Fetched(byId(toResponses(response, PlayerResponse.class)), List.of(new Item(response, ALL_IDS)));
                })
        );
    }

    @Test
    void 비로그인은_앞_4자리만_보고_전체_학번은_본문에_없다() {
        for (Source source : sources()) {
            Fetched fetched = source.fetch().apply(null);
            assertDisclosure(source, fetched, Set.of());
        }
    }

    @Test
    void 다른_학교_매니저는_마스킹된다() {
        for (Source source : sources()) {
            assertDisclosure(source, source.fetch().apply("other-school@example.com"), Set.of(OTHER_SCHOOL_PLAYER));
        }
    }

    @Test
    void 같은_학교_매니저는_자기_학교_선수의_전체_학번을_본다() {
        for (Source source : sources()) {
            assertDisclosure(source, source.fetch().apply("same-school@example.com"),
                    Set.of(1L, 2L, 3L, 4L, 5L, 6L));
        }
    }

    @Test
    void 슈퍼유저는_전체_학번을_본다() {
        for (Source source : sources()) {
            assertDisclosure(source, source.fetch().apply("super@example.com"),
                    Set.of(1L, 2L, 3L, 4L, 5L, 6L, 7L));
        }
    }

    @Test
    void 다른_학교_소속이어도_그_학교_대회_관리자면_전체_학번을_본다() {
        for (Source source : sources()) {
            assertDisclosure(source, source.fetch().apply("league-admin@example.com"),
                    Set.of(1L, 2L, 3L, 4L, 5L, 6L));
        }
    }

    private void assertDisclosure(Source source, Fetched fetched, Set<Long> revealed) {
        String label = source.name();
        assertThat(fetched.players()).as(label).hasSize(ORIGINAL.size());
        for (Long playerId : ORIGINAL.keySet()) {
            PlayerResponse actual = fetched.players().get(playerId);
            boolean full = revealed.contains(playerId);
            String expected = full ? ORIGINAL.get(playerId) : MASKED.get(playerId);
            assertThat(actual.studentNumber()).as(label + " 선수 " + playerId).isEqualTo(expected);
            assertThat(actual.admissionYear()).as(label + " 입학연도 " + playerId)
                    .isEqualTo(ADMISSION_YEAR.get(playerId));
            if (actual.studentNumber() != null && ORIGINAL.get(playerId).length() >= 4) {
                // 관중 화면과 앱은 substring(2, 4) 로 입학연도를 그린다
                assertThat(actual.studentNumber().substring(2, 4)).as(label + " substring " + playerId)
                        .isEqualTo(ORIGINAL.get(playerId).substring(2, 4));
                assertThat(actual.studentNumber()).as(label + " 길이 " + playerId)
                        .hasSize(ORIGINAL.get(playerId).length());
            }
        }
        for (Item item : fetched.items()) {
            ExtractableResponse<Response> response = item.response();
            boolean anyRevealed = item.playerIds().stream()
                    .anyMatch(id -> revealed.contains(id) && ORIGINAL.get(id) != null);
            assertThat(response.statusCode()).as(label).isEqualTo(200);
            String cacheControl = String.valueOf(response.header("Cache-Control"));
            if (anyRevealed) {
                assertThat(cacheControl).as(label + " Cache-Control").contains("private");
                assertThat(response.headers().getValues("Vary")).as(label + " Vary").contains("Cookie");
            } else {
                assertThat(cacheControl).as(label + " Cache-Control").doesNotContain("private");
            }
            for (Long playerId : ORIGINAL.keySet()) {
                String original = ORIGINAL.get(playerId);
                if (original != null && original.length() >= 9 && !revealed.contains(playerId)) {
                    assertThat(response.asString()).as(label + " 본문에 전체 학번 " + playerId)
                            .doesNotContain(original);
                }
            }
        }
    }

    private Fetched fetchPlayerDetails(String email) {
        Map<Long, PlayerResponse> players = new LinkedHashMap<>();
        List<Item> items = new java.util.ArrayList<>();
        for (Long playerId : ORIGINAL.keySet()) {
            ExtractableResponse<Response> response = get(email, "/players/{id}", playerId);
            players.put(playerId, toResponse(response, PlayerResponse.class));
            items.add(new Item(response, List.of(playerId)));
        }
        return new Fetched(players, items);
    }

    private Map<Long, PlayerResponse> byId(List<PlayerResponse> players) {
        Map<Long, PlayerResponse> map = new LinkedHashMap<>();
        players.forEach(p -> map.put(p.playerId(), p));
        return map;
    }

    private ExtractableResponse<Response> get(String email, String path, Long id) {
        var request = RestAssured.given().contentType(MediaType.APPLICATION_JSON_VALUE);
        if (email != null) {
            configureMockJwtForEmail(email);
            request = request.cookie(COOKIE_NAME, mockToken);
        }
        return request.when().get(path, id).then().extract();
    }
}
