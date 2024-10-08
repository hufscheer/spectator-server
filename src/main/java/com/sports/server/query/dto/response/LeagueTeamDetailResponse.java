package com.sports.server.query.dto.response;

import com.sports.server.command.leagueteam.domain.LeagueTeam;
import com.sports.server.command.leagueteam.domain.LeagueTeamPlayer;
import java.util.List;

public record LeagueTeamDetailResponse(
        String logoImageUrl,
        String teamName,
        List<LeagueTeamPlayerResponse> leagueTeamPlayers
) {
    public static LeagueTeamDetailResponse of(final LeagueTeam leagueTeam,
                                              final List<LeagueTeamPlayer> leagueTeamPlayers) {
        return new LeagueTeamDetailResponse(
                leagueTeam.getLogoImageUrl(), leagueTeam.getName(),
                leagueTeamPlayers.stream()
                        .map(LeagueTeamPlayerResponse::of)
                        .toList()
        );
    }

    public record LeagueTeamPlayerResponse(
            Long id,
            String name,
            int number
    ) {
        public static LeagueTeamPlayerResponse of(final LeagueTeamPlayer leagueTeamPlayer) {
            return new LeagueTeamPlayerResponse(
                    leagueTeamPlayer.getId(), leagueTeamPlayer.getName(), leagueTeamPlayer.getNumber()
            );
        }
    }
}
