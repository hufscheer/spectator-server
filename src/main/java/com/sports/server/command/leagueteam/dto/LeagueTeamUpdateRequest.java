package com.sports.server.command.leagueteam.dto;

import com.sports.server.command.leagueteam.domain.LeagueTeam;
import com.sports.server.command.leagueteam.domain.LeagueTeamPlayer;
import java.util.List;

public record LeagueTeamUpdateRequest(
        String name,
        String logoImageUrl,
        List<LeagueTeamPlayerRegisterRequest> players,
        List<Long> deletedPlayerIds

) {
    public record LeagueTeamPlayerRegisterRequest(
            String name,
            int number
    ) {
        public LeagueTeamPlayer toEntity(LeagueTeam leagueTeam) {
            return new LeagueTeamPlayer(leagueTeam, name, number);
        }
    }
}
