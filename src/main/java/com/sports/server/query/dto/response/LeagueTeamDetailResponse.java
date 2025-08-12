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
//    public static LeagueTeamDetailResponse of(final LeagueTeam leagueTeam, final List<LeagueTeamPlayer> leagueTeamPlayers) {
//        Team team = leagueTeam.getTeam();
//        return new LeagueTeamDetailResponse(
//                team.getLogoImageUrl(),
//                team.getName(),
//                team.getTeamColor(),
//                leagueTeamPlayers.stream()
//                        .map(PlayerResponse::of)
//                        .toList()
//        );
//    }
//}
