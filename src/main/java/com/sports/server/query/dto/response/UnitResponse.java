package com.sports.server.query.dto.response;

import com.sports.server.command.team.domain.Unit;

public record UnitResponse(
        String unit,
        String unitName,
        boolean hasTeam
) {
    public static UnitResponse of(Unit unit, boolean hasTeam) {
        return new UnitResponse(unit.name(), unit.getName(), hasTeam);
    }
}
