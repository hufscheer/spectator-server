package com.sports.server.command.cheertalk.dto;

import com.sports.server.command.cheertalk.domain.BotType;
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
            throw new IllegalArgumentException("지원하지 않는 BotType 입니다: " + botType);
        }
        return mapper;
    }
}
