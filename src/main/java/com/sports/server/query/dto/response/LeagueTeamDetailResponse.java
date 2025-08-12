//package com.sports.server.query.dto.response;
//
//
//import com.sports.server.command.league.domain.LeagueTeam;
//import com.sports.server.command.team.domain.Team;
//
//import java.util.List;
//
//public record LeagueTeamDetailResponse(
//        String logoImageUrl,
//        String teamName,
//        String teamColor,
//        List<PlayerResponse> players
//) {
//    public static LeagueTeamDetailResponse of(final LeagueTeam leagueTeam, final List<PlayerResponse> players) {
//        Team team = leagueTeam.getTeam();
//        return new LeagueTeamDetailResponse(
//                team.getLogoImageUrl(),
//                team.getName(),
//                team.getTeamColor(),
//                players
//        );
//    }
//}
// TODO: 필요없으면 삭제
