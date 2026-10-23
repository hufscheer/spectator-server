package com.sports.server.query.dto.response;

import com.sports.server.command.team.domain.Team;
import com.sports.server.common.util.StudentNumber;
import com.sports.server.query.dto.PlayerAssistCountWithRank;

public record TopAssistResponse(
        Long playerId,
        String playerName,
        String admissionYear,
        Integer ranking,
        Integer assistCount,
        Long teamId,
        String teamName,
        String logoImageUrl
) {
    public static TopAssistResponse of(PlayerAssistCountWithRank assist, Team team) {
        return new TopAssistResponse(
                assist.playerId(),
                assist.playerName(),
                StudentNumber.extractAdmissionYear(assist.studentNumber()),
                assist.rank().intValue(),
                assist.assistCount().intValue(),
                team == null ? null : team.getId(),
                team == null ? null : team.getName(),
                team == null ? null : team.getLogoImageUrl()
        );
    }
}
