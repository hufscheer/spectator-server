package com.sports.server.command.bracket.domain;

import static com.sports.server.support.fixture.FixtureMonkeyUtils.entityBuilder;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.sports.server.command.bracket.exception.BracketErrorMessages;
import com.sports.server.command.game.domain.Game;
import com.sports.server.command.game.domain.GameResult;
import com.sports.server.command.game.domain.GameState;
import com.sports.server.command.game.domain.GameTeam;
import com.sports.server.command.league.domain.League;
import com.sports.server.command.league.domain.Round;
import com.sports.server.command.team.domain.Team;
import com.sports.server.common.exception.BadRequestException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class BracketTest {

    private League league;
    private Map<Long, Team> teams;

    @BeforeEach
    void setUp() {
        league = entityBuilder(League.class).set("id", 1L).set("thirdPlaceMatchEnabled", false).sample();
        teams = new HashMap<>();
        for (long id = 1; id <= 8; id++) {
            teams.put(id, entityBuilder(Team.class).set("id", id).set("name", "팀" + id).sample());
        }
    }

    private Map<Integer, Team> placementsOf(long... teamIds) {
        Map<Integer, Team> placements = new HashMap<>();
        for (int position = 1; position <= teamIds.length; position++) {
            if (teamIds[position - 1] != 0) {
                placements.put(position, teams.get(teamIds[position - 1]));
            }
        }
        return placements;
    }

    private BracketMatch matchOf(List<BracketMatch> matches, int roundNumber, int matchNumber) {
        return matches.stream()
                .filter(m -> m.getRound().getNumber() == roundNumber && m.getMatchNumber() == matchNumber)
                .findAny()
                .orElseThrow();
    }

    private Game finishedGame(Team winner, Team loser) {
        return gameOf(GameState.FINISHED, winner, GameResult.WIN, loser, GameResult.LOSE);
    }

    private Game drawGame(Team team1, Team team2) {
        return gameOf(GameState.FINISHED, team1, GameResult.DRAW, team2, GameResult.DRAW);
    }

    private Game gameOf(GameState state, Team team1, GameResult result1, Team team2, GameResult result2) {
        Game game = entityBuilder(Game.class)
                .set("state", state)
                .set("gameTeams", new ArrayList<>())
                .sample();
        game.addGameTeam(entityBuilder(GameTeam.class)
                .set("id", 1L).set("game", game).set("team", team1).set("result", result1).sample());
        game.addGameTeam(entityBuilder(GameTeam.class)
                .set("id", 2L).set("game", game).set("team", team2).set("result", result2).sample());
        return game;
    }

    @Nested
    @DisplayName("대진표를 생성할 때")
    class GenerateTest {

        @Test
        void 첫_라운드부터_결승까지_모든_매치가_생성된다() {
            // when
            List<BracketMatch> matches = Bracket.generate(league, 8, placementsOf(1, 2, 3, 4, 5, 6, 7, 8));

            // then
            assertAll(
                    () -> assertThat(matches).hasSize(7),
                    () -> assertThat(matches.stream()
                            .filter(m -> m.getRound() == Round.QUARTER_FINAL)).hasSize(4),
                    () -> assertThat(matches.stream()
                            .filter(m -> m.getRound() == Round.SEMI_FINAL)).hasSize(2),
                    () -> assertThat(matches.stream()
                            .filter(m -> m.getRound() == Round.FINAL)).hasSize(1)
            );
        }

        @Test
        void 첫_라운드_매치에_위치_순서대로_팀이_배치된다() {
            // when
            List<BracketMatch> matches = Bracket.generate(league, 8, placementsOf(1, 2, 3, 4, 5, 6, 7, 8));

            // then
            BracketMatch firstMatch = matchOf(matches, 8, 1);
            BracketMatch lastMatch = matchOf(matches, 8, 4);
            assertAll(
                    () -> assertThat(firstMatch.getTeam1()).isEqualTo(teams.get(1L)),
                    () -> assertThat(firstMatch.getTeam2()).isEqualTo(teams.get(2L)),
                    () -> assertThat(lastMatch.getTeam1()).isEqualTo(teams.get(7L)),
                    () -> assertThat(lastMatch.getTeam2()).isEqualTo(teams.get(8L))
            );
        }

        @Test
        void 배치되지_않은_위치는_비어있다() {
            // when (2번, 8번 위치 부전승)
            List<BracketMatch> matches = Bracket.generate(league, 8, placementsOf(1, 0, 3, 4, 5, 6, 7));

            // then
            assertAll(
                    () -> assertThat(matchOf(matches, 8, 1).getTeam2()).isNull(),
                    () -> assertThat(matchOf(matches, 8, 4).getTeam2()).isNull()
            );
        }

        @Test
        void 상위_라운드_매치에는_팀이_배치되지_않는다() {
            // when
            List<BracketMatch> matches = Bracket.generate(league, 8, placementsOf(1, 2, 3, 4, 5, 6, 7, 8));

            // then
            BracketMatch semiFinal = matchOf(matches, 4, 1);
            assertAll(
                    () -> assertThat(semiFinal.getTeam1()).isNull(),
                    () -> assertThat(semiFinal.getTeam2()).isNull()
            );
        }
    }

    @Nested
    @DisplayName("경기와 매치를 매칭할 때")
    class FindMeetingMatchTest {

        private Bracket bracket;
        private List<BracketMatch> matches;

        @BeforeEach
        void setUp() {
            matches = Bracket.generate(league, 8, placementsOf(1, 2, 3, 4, 5, 6, 7, 8));
            bracket = Bracket.from(matches);
        }

        @Test
        void 첫_라운드에서_만나는_두_팀의_매치를_찾는다() {
            // when
            Optional<BracketMatch> match = bracket.findMeetingMatch(Round.QUARTER_FINAL, 1L, 2L);

            // then
            assertThat(match).contains(matchOf(matches, 8, 1));
        }

        @Test
        void 팀_순서가_바뀌어도_같은_매치를_찾는다() {
            // when
            Optional<BracketMatch> match = bracket.findMeetingMatch(Round.QUARTER_FINAL, 2L, 1L);

            // then
            assertThat(match).contains(matchOf(matches, 8, 1));
        }

        @Test
        void 첫_라운드에서_만나지_않는_조합은_매치가_없다() {
            // when
            Optional<BracketMatch> match = bracket.findMeetingMatch(Round.QUARTER_FINAL, 1L, 3L);

            // then
            assertThat(match).isEmpty();
        }

        @Test
        void 준결승에서_만나는_조합은_준결승_매치를_찾는다() {
            // when
            Optional<BracketMatch> match = bracket.findMeetingMatch(Round.SEMI_FINAL, 1L, 4L);

            // then
            assertThat(match).contains(matchOf(matches, 4, 1));
        }

        @Test
        void 준결승이라도_같은_사이드_조합은_매치가_없다() {
            // when (1번과 2번은 8강에서 만나는 조합)
            Optional<BracketMatch> match = bracket.findMeetingMatch(Round.SEMI_FINAL, 1L, 2L);

            // then
            assertThat(match).isEmpty();
        }

        @Test
        void 결승에서_만나는_조합은_결승_매치를_찾는다() {
            // when
            Optional<BracketMatch> match = bracket.findMeetingMatch(Round.FINAL, 1L, 5L);

            // then
            assertThat(match).contains(matchOf(matches, 2, 1));
        }

        @Test
        void 배치되지_않은_팀은_매치를_찾을_수_없다() {
            // when
            Optional<BracketMatch> match = bracket.findMeetingMatch(Round.QUARTER_FINAL, 1L, 99L);

            // then
            assertThat(match).isEmpty();
        }

        @Test
        void 대진표에_없는_라운드는_매치를_찾을_수_없다() {
            // when
            Optional<BracketMatch> match = bracket.findMeetingMatch(Round.PRELIMINARY, 1L, 2L);

            // then
            assertThat(match).isEmpty();
        }
    }

    @Nested
    @DisplayName("진출팀을 계산할 때")
    class AdvancerTest {

        @Test
        void 경기_승자가_다음_라운드_슬롯에_반영된다() {
            // given
            List<BracketMatch> matches = Bracket.generate(league, 8, placementsOf(1, 2, 3, 4, 5, 6, 7, 8));
            BracketMatch firstMatch = matchOf(matches, 8, 1);
            firstMatch.linkGame(finishedGame(teams.get(1L), teams.get(2L)));
            Bracket bracket = Bracket.from(matches);

            // when & then
            assertAll(
                    () -> assertThat(bracket.advancerOf(firstMatch)).isEqualTo(teams.get(1L)),
                    () -> assertThat(bracket.slotOf(matchOf(matches, 4, 1), Bracket.TEAM1_SIDE))
                            .isEqualTo(teams.get(1L)),
                    () -> assertThat(bracket.slotOf(matchOf(matches, 4, 1), Bracket.TEAM2_SIDE)).isNull()
            );
        }

        @Test
        void 무승부인_경기는_진출팀이_없다() {
            // given
            List<BracketMatch> matches = Bracket.generate(league, 8, placementsOf(1, 2, 3, 4, 5, 6, 7, 8));
            BracketMatch firstMatch = matchOf(matches, 8, 1);
            firstMatch.linkGame(
                    gameOf(GameState.FINISHED, teams.get(1L), GameResult.DRAW, teams.get(2L), GameResult.DRAW));
            Bracket bracket = Bracket.from(matches);

            // when & then
            assertThat(bracket.advancerOf(firstMatch)).isNull();
        }

        @Test
        void 종료되지_않은_경기는_진출팀이_없다() {
            // given
            List<BracketMatch> matches = Bracket.generate(league, 8, placementsOf(1, 2, 3, 4, 5, 6, 7, 8));
            BracketMatch firstMatch = matchOf(matches, 8, 1);
            firstMatch.linkGame(gameOf(GameState.PLAYING, teams.get(1L), null, teams.get(2L), null));
            Bracket bracket = Bracket.from(matches);

            // when & then
            assertThat(bracket.advancerOf(firstMatch)).isNull();
        }

        @Test
        void 상대가_없는_팀은_부전승으로_진출한다() {
            // given (2번 위치 부전승)
            List<BracketMatch> matches = Bracket.generate(league, 8, placementsOf(1, 0, 3, 4, 5, 6, 7, 8));
            Bracket bracket = Bracket.from(matches);

            // when & then
            assertAll(
                    () -> assertThat(bracket.advancerOf(matchOf(matches, 8, 1))).isEqualTo(teams.get(1L)),
                    () -> assertThat(bracket.slotOf(matchOf(matches, 4, 1), Bracket.TEAM1_SIDE))
                            .isEqualTo(teams.get(1L))
            );
        }

        @Test
        void 연쇄_부전승이면_상위_라운드까지_진출한다() {
            // given (1, 2번 위치가 모두 비고 3번만 배치)
            List<BracketMatch> matches = Bracket.generate(league, 8, placementsOf(0, 0, 3, 0, 5, 6, 7, 8));
            Bracket bracket = Bracket.from(matches);

            // when & then (8강 2매치 부전승 → 준결승 1매치도 상대 없음 → 결승 슬롯까지 진출)
            assertAll(
                    () -> assertThat(bracket.advancerOf(matchOf(matches, 8, 2))).isEqualTo(teams.get(3L)),
                    () -> assertThat(bracket.advancerOf(matchOf(matches, 4, 1))).isEqualTo(teams.get(3L)),
                    () -> assertThat(bracket.slotOf(matchOf(matches, 2, 1), Bracket.TEAM1_SIDE))
                            .isEqualTo(teams.get(3L))
            );
        }

        @Test
        void 양쪽이_모두_비어있는_매치는_진출팀이_없다() {
            // given
            List<BracketMatch> matches = Bracket.generate(league, 8, placementsOf(0, 0, 3, 4, 5, 6, 7, 8));
            Bracket bracket = Bracket.from(matches);

            // when & then
            assertThat(bracket.advancerOf(matchOf(matches, 8, 1))).isNull();
        }

        @Test
        void 아직_경기가_없는_매치는_상대_서브트리에_팀이_있으면_진출팀이_없다() {
            // given (8강 1매치는 승자 확정, 8강 2매치는 미진행 → 준결승 진출팀 미확정)
            List<BracketMatch> matches = Bracket.generate(league, 8, placementsOf(1, 2, 3, 4, 5, 6, 7, 8));
            matchOf(matches, 8, 1).linkGame(finishedGame(teams.get(1L), teams.get(2L)));
            Bracket bracket = Bracket.from(matches);

            // when & then
            assertThat(bracket.advancerOf(matchOf(matches, 4, 1))).isNull();
        }
    }

    @Nested
    @DisplayName("3·4위전을 진행하는 대회에서")
    class ThirdPlaceTest {

        private League thirdPlaceLeague;

        @BeforeEach
        void setUp() {
            thirdPlaceLeague = entityBuilder(League.class)
                    .set("id", 2L)
                    .set("thirdPlaceMatchEnabled", true)
                    .sample();
        }

        private BracketMatch thirdPlaceOf(List<BracketMatch> matches) {
            return matches.stream()
                    .filter(match -> match.getRound() == Round.THIRD_PLACE_MATCH)
                    .findAny()
                    .orElse(null);
        }

        private List<BracketMatch> semiFinalsPlayed(Map<Integer, Team> placements) {
            List<BracketMatch> matches = Bracket.generate(thirdPlaceLeague, 4, placements);
            matchOf(matches, 4, 1).linkGame(finishedGame(teams.get(1L), teams.get(2L)));
            matchOf(matches, 4, 2).linkGame(finishedGame(teams.get(3L), teams.get(4L)));
            return matches;
        }

        @Test
        void 트리와_별도로_3_4위전_매치가_생성된다() {
            // when
            List<BracketMatch> matches = Bracket.generate(thirdPlaceLeague, 4, placementsOf(1, 2, 3, 4));

            // then
            assertThat(thirdPlaceOf(matches)).isNotNull();
        }

        @Test
        void 대회가_3_4위전을_진행하지_않으면_매치가_생성되지_않는다() {
            // when
            List<BracketMatch> matches = Bracket.generate(league, 4, placementsOf(1, 2, 3, 4));

            // then
            assertThat(thirdPlaceOf(matches)).isNull();
        }

        @Test
        void 준결승이_없는_크기에서는_3_4위전_매치가_생성되지_않는다() {
            // when (2팀 대회는 결승뿐이라 준결승 패자가 없다)
            List<BracketMatch> matches = Bracket.generate(thirdPlaceLeague, 2, placementsOf(1, 2));

            // then
            assertThat(thirdPlaceOf(matches)).isNull();
        }

        @Test
        void 부속_매치는_트리_라운드_목록에_포함되지_않는다() {
            // given
            List<BracketMatch> matches = Bracket.generate(thirdPlaceLeague, 4, placementsOf(1, 2, 3, 4));

            // when
            Bracket bracket = Bracket.from(matches);

            // then
            assertAll(
                    () -> assertThat(bracket.roundNumbers()).containsExactly(4, 2),
                    () -> assertThat(bracket.getThirdPlaceMatch()).isNotNull()
            );
        }

        @Test
        void 참가팀은_준결승_패자로_유도된다() {
            // given
            Bracket bracket = Bracket.from(semiFinalsPlayed(placementsOf(1, 2, 3, 4)));

            // when & then
            assertAll(
                    () -> assertThat(bracket.thirdPlaceSlotOf(Bracket.TEAM1_SIDE)).isEqualTo(teams.get(2L)),
                    () -> assertThat(bracket.thirdPlaceSlotOf(Bracket.TEAM2_SIDE)).isEqualTo(teams.get(4L))
            );
        }

        @Test
        void 준결승이_끝나지_않으면_참가팀이_미확정이다() {
            // given
            Bracket bracket = Bracket.from(Bracket.generate(thirdPlaceLeague, 4, placementsOf(1, 2, 3, 4)));

            // when & then
            assertAll(
                    () -> assertThat(bracket.thirdPlaceSlotOf(Bracket.TEAM1_SIDE)).isNull(),
                    () -> assertThat(bracket.thirdPlaceSlotOf(Bracket.TEAM2_SIDE)).isNull()
            );
        }

        @Test
        void 준결승_패자_두_팀이면_검증을_통과한다() {
            // given
            Bracket bracket = Bracket.from(semiFinalsPlayed(placementsOf(1, 2, 3, 4)));

            // when & then
            assertThatNoException()
                    .isThrownBy(() -> bracket.validateThirdPlaceContenders(2L, 4L));
        }

        @Test
        void 팀_순서가_바뀌어도_검증을_통과한다() {
            // given
            Bracket bracket = Bracket.from(semiFinalsPlayed(placementsOf(1, 2, 3, 4)));

            // when & then
            assertThatNoException()
                    .isThrownBy(() -> bracket.validateThirdPlaceContenders(4L, 2L));
        }

        @Test
        void 준결승_패자가_아닌_팀이_섞이면_검증에_실패한다() {
            // given
            Bracket bracket = Bracket.from(semiFinalsPlayed(placementsOf(1, 2, 3, 4)));

            // when & then
            assertThatThrownBy(() -> bracket.validateThirdPlaceContenders(1L, 4L))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining(BracketErrorMessages.THIRD_PLACE_TEAMS_MISMATCH);
        }

        @Test
        void 준결승이_끝나지_않았으면_검증에_실패한다() {
            // given
            Bracket bracket = Bracket.from(Bracket.generate(thirdPlaceLeague, 4, placementsOf(1, 2, 3, 4)));

            // when & then
            assertThatThrownBy(() -> bracket.validateThirdPlaceContenders(1L, 3L))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining(BracketErrorMessages.SEMI_FINALS_NOT_FINISHED);
        }

        @Test
        void 준결승_한_경기만_끝났으면_검증에_실패한다() {
            // given (2번 팀은 확정된 패자지만 3번 팀은 아직 준결승을 뛰고 있다)
            List<BracketMatch> matches = Bracket.generate(thirdPlaceLeague, 4, placementsOf(1, 2, 3, 4));
            matchOf(matches, 4, 1).linkGame(finishedGame(teams.get(1L), teams.get(2L)));
            Bracket bracket = Bracket.from(matches);

            // when & then
            assertThatThrownBy(() -> bracket.validateThirdPlaceContenders(2L, 3L))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining(BracketErrorMessages.SEMI_FINALS_NOT_FINISHED);
        }

        @Test
        void 무승부로_남은_준결승은_패자가_없어_검증을_건너뛴다() {
            // given
            List<BracketMatch> matches = Bracket.generate(thirdPlaceLeague, 4, placementsOf(1, 2, 3, 4));
            matchOf(matches, 4, 1).linkGame(drawGame(teams.get(1L), teams.get(2L)));
            matchOf(matches, 4, 2).linkGame(finishedGame(teams.get(3L), teams.get(4L)));
            Bracket bracket = Bracket.from(matches);

            // when & then
            assertThatNoException()
                    .isThrownBy(() -> bracket.validateThirdPlaceContenders(1L, 4L));
        }
    }
}
