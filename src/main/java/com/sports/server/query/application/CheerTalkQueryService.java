package com.sports.server.query.application;

import com.sports.server.command.cheertalk.domain.CheerTalk;
import com.sports.server.common.dto.PageRequestDto;
import com.sports.server.query.dto.response.CheerTalkResponse;
import com.sports.server.query.repository.CheerTalkDynamicRepository;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CheerTalkQueryService {

    private final CheerTalkDynamicRepository cheerTalkDynamicRepository;

    public List<CheerTalkResponse> getCheerTalksByGameId(final Long gameId, final PageRequestDto pageRequest) {
        List<CheerTalk> cheerTalks = cheerTalkDynamicRepository.findByGameIdOrderByStartTime(
                gameId, pageRequest.cursor(), pageRequest.size()
        );

        List<CheerTalkResponse> responses = cheerTalks.stream()
                .map(CheerTalkResponse::new)
                .collect(Collectors.toList());

        Collections.reverse(responses);
        return responses;
    }

    private List<Long> getOrderedGameTeamIds(final List<CheerTalk> cheerTalks) {
        return cheerTalks.stream()
                .map(CheerTalk::getGameTeamId)
                .collect(Collectors.toSet())
                .stream()
                .sorted()
                .toList();
    }
}
