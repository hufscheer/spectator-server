package com.sports.server.command.leagueteam.application;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.sports.server.command.league.domain.League;
import com.sports.server.command.leagueteam.domain.LeagueTeam;
import com.sports.server.command.leagueteam.domain.LeagueTeamRepository;
import com.sports.server.command.leagueteam.dto.LeagueTeamRegisterRequest;
import com.sports.server.command.leagueteam.dto.LeagueTeamRegisterRequest.LeagueTeamPlayerRegisterRequest;
import com.sports.server.command.member.domain.Member;
import com.sports.server.common.application.EntityUtils;
import com.sports.server.common.exception.UnauthorizedException;
import com.sports.server.support.ServiceTest;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

@Sql("/league-fixture.sql")
public class LeagueTeamServiceTest extends ServiceTest {

    @Autowired
    private EntityUtils entityUtils;

    @Autowired
    private LeagueTeamService leagueTeamService;

    @Autowired
    private LeagueTeamRepository leagueTeamRepository;

    @Test
    void 리그의_매니저가_아닌_회원이_리그팀을_등록하려고_하면_예외가_발생한다() {
        // given
        Long leagueId = 1L;
        Member nonManager = entityUtils.getEntity(2L, Member.class);
        LeagueTeamRegisterRequest request = new LeagueTeamRegisterRequest("name", "logo-image", List.of());

        // when & then
        assertThrows(UnauthorizedException.class, () -> {
            leagueTeamService.register(leagueId, nonManager, request);
        });
    }

    @Test
    void 정상적으로_리그팀이_등록된다() {
        // given
        Long leagueId = 1L;
        League league = entityUtils.getEntity(1L, League.class);
        Member manager = entityUtils.getEntity(1L, Member.class);
        String leagueTeamName = "name";
        List<LeagueTeamPlayerRegisterRequest> playerRegisterRequests = List.of(
                new LeagueTeamPlayerRegisterRequest("name-a", 1),
                new LeagueTeamPlayerRegisterRequest("name-b", 2));
        LeagueTeamRegisterRequest request = new LeagueTeamRegisterRequest(leagueTeamName, "logo-image-url",
                playerRegisterRequests);

        // when
        leagueTeamService.register(leagueId, manager, request);

        // then
        Optional<LeagueTeam> savedLeagueTeamOptional = leagueTeamRepository.findByLeagueAndName(league,
                leagueTeamName);
        assertTrue(savedLeagueTeamOptional.isPresent(), "리그팀이 저장되지 않았습니다.");

        LeagueTeam savedLeagueTeam = savedLeagueTeamOptional.get();
        assertEquals(leagueTeamName, savedLeagueTeam.getName());
    }
}
