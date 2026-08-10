package com.sports.server.command.bracket.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.sports.server.command.bracket.domain.BracketMatch;
import com.sports.server.command.bracket.domain.BracketMatchRepository;
import com.sports.server.command.bracket.dto.BracketRequest;
import com.sports.server.command.bracket.exception.BracketErrorMessages;
import com.sports.server.command.game.application.GameService;
import com.sports.server.command.game.domain.Game;
import com.sports.server.command.game.dto.GameRequest;
import com.sports.server.command.league.application.LeagueService;
import com.sports.server.command.league.domain.League;
import com.sports.server.command.league.domain.Round;
import com.sports.server.command.league.dto.LeagueRequest;
import com.sports.server.command.league.exception.LeagueErrorMessages;
import com.sports.server.command.member.domain.Member;
import com.sports.server.common.application.EntityUtils;
import com.sports.server.common.exception.BadRequestException;
import com.sports.server.common.exception.ExceptionMessages;
import com.sports.server.common.exception.UnauthorizedException;
import com.sports.server.support.ServiceTest;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

@Sql("/bracket-fixture.sql")
public class BracketServiceTest extends ServiceTest {

    @Autowired
    private BracketService bracketService;

    @Autowired
    private BracketMatchRepository bracketMatchRepository;

    @Autowired
    private GameService gameService;

    @Autowired
    private LeagueService leagueService;

    @Autowired
    private EntityUtils entityUtils;

    private Member manager() {
        return entityUtils.getEntity(1L, Member.class);
    }

    private BracketRequest.Save saveRequestOf(int size, BracketRequest.Entry... entries) {
        return new BracketRequest.Save(size, List.of(entries));
    }

    private BracketRequest.Entry entry(int position, Long teamId) {
        return new BracketRequest.Entry(position, teamId);
    }

    @Nested
    @DisplayName("대진표를 배치할 때")
    class ReplaceTest {

        @Test
        void 대진표가_없던_리그에_대진표를_생성한다() {
            // given
            BracketRequest.Save request = saveRequestOf(8,
                    entry(1, 1L), entry(2, 2L), entry(3, 3L), entry(4, 4L), entry(5, 5L), entry(7, 6L));

            // when
            bracketService.replace(manager(), 1L, request);

            // then
            List<BracketMatch> matches = bracketMatchRepository.findAllByLeagueId(1L);
            BracketMatch firstMatch = matches.stream()
                    .filter(m -> m.getRound() == Round.QUARTER_FINAL && m.getMatchNumber() == 1)
                    .findAny().orElseThrow();
            assertThat(matches).hasSize(7);
            assertThat(firstMatch.getTeam1().getId()).isEqualTo(1L);
            assertThat(firstMatch.getTeam2().getId()).isEqualTo(2L);
        }

        @Test
        void 기존_대진표는_새_배치로_교체된다() {
            // given (리그 2는 4강 대진표 보유, 연결된 경기 없음)
            BracketRequest.Save request = saveRequestOf(4,
                    entry(1, 4L), entry(2, 3L), entry(3, 2L), entry(4, 1L));

            // when
            bracketService.replace(manager(), 2L, request);

            // then
            List<BracketMatch> matches = bracketMatchRepository.findAllByLeagueId(2L);
            BracketMatch firstMatch = matches.stream()
                    .filter(m -> m.getRound() == Round.SEMI_FINAL && m.getMatchNumber() == 1)
                    .findAny().orElseThrow();
            assertThat(matches).hasSize(3);
            assertThat(firstMatch.getTeam1().getId()).isEqualTo(4L);
            assertThat(firstMatch.getTeam2().getId()).isEqualTo(3L);
        }

        @Test
        void 리그에_참가하지_않은_팀은_배치할_수_없다() {
            // given (7번 팀은 리그 2 미참가)
            BracketRequest.Save request = saveRequestOf(4, entry(1, 1L), entry(2, 7L));

            // when & then
            assertThatThrownBy(() -> bracketService.replace(manager(), 2L, request))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage(LeagueErrorMessages.TEAMS_NOT_IN_LEAGUE_TEAM_EXCEPTION);
        }

        @Test
        void 중복된_위치에는_배치할_수_없다() {
            // given
            BracketRequest.Save request = saveRequestOf(4, entry(1, 1L), entry(1, 2L));

            // when & then
            assertThatThrownBy(() -> bracketService.replace(manager(), 2L, request))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage(BracketErrorMessages.DUPLICATED_POSITION);
        }

        @Test
        void 한_팀을_두_위치에_배치할_수_없다() {
            // given
            BracketRequest.Save request = saveRequestOf(4, entry(1, 1L), entry(2, 1L));

            // when & then
            assertThatThrownBy(() -> bracketService.replace(manager(), 2L, request))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage(BracketErrorMessages.DUPLICATED_TEAM);
        }

        @Test
        void 위치가_대진표_크기를_벗어나면_배치할_수_없다() {
            // given
            BracketRequest.Save request = saveRequestOf(4, entry(1, 1L), entry(5, 2L));

            // when & then
            assertThatThrownBy(() -> bracketService.replace(manager(), 2L, request))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage(BracketErrorMessages.POSITION_OUT_OF_RANGE);
        }

        @Test
        void 대진표_크기는_리그의_최대_라운드를_넘을_수_없다() {
            // given (리그 2의 최대 라운드는 4강)
            BracketRequest.Save request = saveRequestOf(8, entry(1, 1L), entry(2, 2L));

            // when & then
            assertThatThrownBy(() -> bracketService.replace(manager(), 2L, request))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage(ExceptionMessages.LEAGUE_ROUND_EXCEEDS_MAX);
        }

        @Test
        void 유효하지_않은_크기로는_생성할_수_없다() {
            // given
            BracketRequest.Save request = saveRequestOf(100, entry(1, 1L), entry(2, 2L));

            // when & then
            assertThatThrownBy(() -> bracketService.replace(manager(), 2L, request))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage(BracketErrorMessages.INVALID_BRACKET_SIZE);
        }

        @Test
        void 두_팀_미만은_배치할_수_없다() {
            // given
            BracketRequest.Save request = saveRequestOf(4, entry(1, 1L));

            // when & then
            assertThatThrownBy(() -> bracketService.replace(manager(), 2L, request))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage(BracketErrorMessages.NOT_ENOUGH_TEAMS);
        }

        @Test
        void 경기가_연결된_대진표는_수정할_수_없다() {
            // given (리그 3의 대진표에는 경기가 연결되어 있음)
            BracketRequest.Save request = saveRequestOf(4, entry(1, 5L), entry(2, 6L));

            // when & then
            assertThatThrownBy(() -> bracketService.replace(manager(), 3L, request))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage(BracketErrorMessages.CANNOT_MODIFY_WITH_LINKED_GAMES);
        }

        @Test
        void 권한이_없는_멤버는_대진표를_배치할_수_없다() {
            // given
            Member nonManager = entityUtils.getEntity(2L, Member.class);
            BracketRequest.Save request = saveRequestOf(4, entry(1, 1L), entry(2, 2L));

            // when & then
            assertThatThrownBy(() -> bracketService.replace(nonManager, 2L, request))
                    .isInstanceOf(UnauthorizedException.class);
        }
    }

    @Nested
    @DisplayName("경기를 대진표에 연결할 때")
    class LinkGameTest {

        @Test
        void 경기_생성_시_팀_조합이_일치하는_매치에_자동으로_연결된다() {
            // given
            GameRequest.Register request = new GameRequest.Register(
                    "4강 1경기", 4, "경기전", "SCHEDULED", LocalDateTime.of(2025, 8, 5, 18, 0), null,
                    new GameRequest.TeamLineupRequest(1L, List.of()),
                    new GameRequest.TeamLineupRequest(2L, List.of())
            , false);

            // when
            Long gameId = gameService.register(2L, request, manager());

            // then
            BracketMatch match = bracketMatchRepository.findById(1L).orElseThrow();
            assertThat(match.getGame().getId()).isEqualTo(gameId);
        }

        @Test
        void 대진표상_만나지_않는_조합은_연결되지_않는다() {
            // given (경기 3: 리그 2에서 1번, 3번 팀의 4강 경기 — 대진표상 4강에서 만나지 않음)
            League league = entityUtils.getEntity(2L, League.class);
            Game game = entityUtils.getEntity(3L, Game.class);

            // when
            bracketService.linkGame(league, game, 1L, 3L);

            // then
            assertThat(bracketMatchRepository.findByGame(game)).isEmpty();
        }

        @Test
        void 결승에서_만나는_조합은_결승_매치에_연결된다() {
            // given (경기 4: 리그 2에서 1번, 3번 팀의 결승 경기)
            League league = entityUtils.getEntity(2L, League.class);
            Game game = entityUtils.getEntity(4L, Game.class);

            // when
            bracketService.linkGame(league, game, 1L, 3L);

            // then
            BracketMatch finalMatch = bracketMatchRepository.findById(3L).orElseThrow();
            assertThat(finalMatch.getGame().getId()).isEqualTo(4L);
        }

        @Test
        void 이미_연결된_매치에는_중복_연결되지_않는다() {
            // given (리그 3의 4강 1매치는 경기 2와 연결됨, 경기 5는 같은 팀 조합의 재경기)
            League league = entityUtils.getEntity(3L, League.class);
            Game rematch = entityUtils.getEntity(5L, Game.class);

            // when
            bracketService.linkGame(league, rematch, 5L, 6L);

            // then
            BracketMatch match = bracketMatchRepository.findById(4L).orElseThrow();
            assertThat(match.getGame().getId()).isEqualTo(2L);
            assertThat(bracketMatchRepository.findByGame(rematch)).isEmpty();
        }

        @Test
        void 경기를_삭제하면_연결이_해제된다() {
            // given (경기 2는 리그 3의 4강 1매치와 연결됨)
            // when
            gameService.deleteGame(3L, 2L, manager());

            // then
            BracketMatch match = bracketMatchRepository.findById(4L).orElseThrow();
            assertThat(match.getGame()).isNull();
        }

        @Test
        void 라운드_변경으로_대진표와_어긋나면_연결이_해제된다() {
            // given (경기 1을 4강 1매치에 연결해둔다)
            League league = entityUtils.getEntity(2L, League.class);
            Game game = entityUtils.getEntity(1L, Game.class);
            bracketService.linkGame(league, game, 1L, 2L);

            // when (1번, 2번 팀은 결승에서 만날 수 없는 조합)
            gameService.updateGame(2L, 1L, new GameRequest.Update(
                    "결승으로 변경", 2, LocalDateTime.of(2025, 8, 10, 18, 0), null, false), manager());

            // then
            assertThat(bracketMatchRepository.findByGame(game)).isEmpty();
        }
    }

    @Nested
    @DisplayName("리그를 생성할 때")
    class RegisterLeagueWithBracketTest {

        @Test
        void 대진표와_함께_생성하면_대진표가_저장된다() {
            // given
            long before = bracketMatchRepository.count();
            LeagueRequest.Register request = new LeagueRequest.Register(
                    "새 대회", 4,
                    LocalDateTime.of(2025, 9, 1, 0, 0), LocalDateTime.of(2025, 9, 15, 0, 0),
                    List.of(7L, 8L), null,
                    saveRequestOf(4, entry(1, 7L), entry(4, 8L))
            , false);

            // when
            leagueService.register(manager(), request);

            // then (4강 2매치 + 결승 1매치)
            assertThat(bracketMatchRepository.count()).isEqualTo(before + 3);
        }

        @Test
        void 대진표_없이_생성하면_대진표가_저장되지_않는다() {
            // given
            long before = bracketMatchRepository.count();
            LeagueRequest.Register request = new LeagueRequest.Register(
                    "새 대회", 4,
                    LocalDateTime.of(2025, 9, 1, 0, 0), LocalDateTime.of(2025, 9, 15, 0, 0),
                    List.of(7L, 8L), null, null
            , false);

            // when
            leagueService.register(manager(), request);

            // then
            assertThat(bracketMatchRepository.count()).isEqualTo(before);
        }
    }

    @Nested
    @DisplayName("리그에서 팀을 제거할 때")
    class RemoveTeamsTest {

        @Test
        void 제거된_팀은_연결되지_않은_매치의_배치에서_빠진다() {
            // given (리그 2의 4강 2매치에 3번, 4번 팀 배치)
            LeagueRequest.Teams request = new LeagueRequest.Teams(List.of(4L));

            // when
            leagueService.removeTeams(manager(), 2L, request);

            // then
            BracketMatch match = bracketMatchRepository.findById(2L).orElseThrow();
            assertThat(match.getTeam1().getId()).isEqualTo(3L);
            assertThat(match.getTeam2()).isNull();
        }
    }
}
