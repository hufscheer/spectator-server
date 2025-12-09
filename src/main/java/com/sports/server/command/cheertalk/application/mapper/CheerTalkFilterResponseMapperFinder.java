package com.sports.server.command.cheertalk.application.mapper;

import com.sports.server.command.cheertalk.domain.BotType;
import com.sports.server.common.exception.BadRequestException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class CheerTalkFilterResponseMapperFinder {

    private final Map<BotType, CheerTalkFilterResponseMapper> mapperMap;

    public CheerTalkFilterResponseMapperFinder(List<CheerTalkFilterResponseMapper> mappers) {
        this.mapperMap = mappers.stream()
                .collect(Collectors.toMap(
                        CheerTalkFilterResponseMapper::supports,
                        Function.identity()
                ));
    }

    public CheerTalkFilterResponseMapper find(BotType botType) {
        CheerTalkFilterResponseMapper mapper = mapperMap.get(botType);
        if (mapper == null) {
            throw new BadRequestException("지원하지 않는 봇 타입입니다.");
        }
        return mapper;
    }
}
