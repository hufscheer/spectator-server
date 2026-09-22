package com.sports.server.query.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.sports.server.command.league.domain.LeagueTeam;
import com.sports.server.command.team.domain.Team;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record LeagueTeamResponse(
        Long teamId,
        Long leagueTeamId,
        String teamName,
        String logoImageUrl,
        Integer sizeOfTeamPlayers,
        Integer cheerCount,
        Integer cheerTalksCount
) {
    public LeagueTeamResponse(final Team team, final Long leagueTeamId) {
        this(
                team.getId(), leagueTeamId, team.getName(),
                team.getLogoImageUrl(), team.getTeamPlayers().size(),
                null, null
        );
    }

    /**
     * 소프트 삭제된 팀을 가리키는 통계는 값 없이 내보낸다.
     *
     * <p>`league_statistics` 는 팀을 FK 로 들고 있는데 {@code Team} 에 `@Where(is_deleted = 0)`
     * 이 걸려 있어, 그 팀이 지워지면 `LEFT JOIN FETCH` 가 연관을 **null 로 채운다**. FK 값은
     * 그대로 남아 있어서 DB 만 보면 멀쩡해 보인다.
     *
     * <p>여기서 null 을 안 막아 운영에서 `GET /leagues/224/statistics` 가 NPE 500 을 냈다
     * (최다 응원 팀 167 이 소프트 삭제된 상태였다). 우승·준우승 쪽은 호출부가 이미 막고 있었다.
     */
    public static LeagueTeamResponse ofWithCheerCount(final LeagueTeam leagueTeam) {
        if (leagueTeam == null) {
            return null;
        }
        return new LeagueTeamResponse(
                leagueTeam.getTeam().getId(),
                leagueTeam.getId(),
                leagueTeam.getTeam().getName(),
                leagueTeam.getTeam().getLogoImageUrl(),
                leagueTeam.getTeam().getTeamPlayers().size(),
                leagueTeam.getTotalCheerCount(),
                null
        );
    }

    /** @see #ofWithCheerCount(LeagueTeam) */
    public static LeagueTeamResponse ofWithTotalTalkCount(final LeagueTeam leagueTeam) {
        if (leagueTeam == null) {
            return null;
        }
        return new LeagueTeamResponse(
                leagueTeam.getTeam().getId(),
                leagueTeam.getId(),
                leagueTeam.getTeam().getName(),
                leagueTeam.getTeam().getLogoImageUrl(),
                leagueTeam.getTeam().getTeamPlayers().size(),
                null,
                leagueTeam.getTotalTalkCount()
        );
    }
}
