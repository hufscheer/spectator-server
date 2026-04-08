package com.sports.server.command.league.domain;

import com.sports.server.common.exception.BadRequestException;
import com.sports.server.common.exception.ExceptionMessages;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class QuarterResolver {

    public static Quarter resolve(String value) {
        return SoccerQuarter.tryResolve(value)
                .<Quarter>map(q -> q)
                .or(() -> BasketballQuarter.tryResolve(value).map(q -> q))
                .orElseThrow(() -> new BadRequestException(
                        String.format(ExceptionMessages.QUARTER_NOT_FOUND_BY_NAME, value)));
    }
}
