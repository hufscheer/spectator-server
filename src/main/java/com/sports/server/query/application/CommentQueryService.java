package com.sports.server.query.application;

import com.sports.server.command.comment.domain.CheerTalk;
import com.sports.server.query.repository.CommentDynamicRepository;
import com.sports.server.query.dto.response.CommentResponse;
import com.sports.server.common.dto.PageRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentQueryService {

    private final CommentDynamicRepository commentDynamicRepository;

    public List<CommentResponse> getCommentsByGameId(final Long gameId, final PageRequestDto pageRequest) {
        List<CheerTalk> cheerTalks = commentDynamicRepository.findByGameIdOrderByStartTime(
                gameId, pageRequest.cursor(), pageRequest.size()
        );

        List<Long> gameTeamIds = getOrderedGameTeamIds(cheerTalks);

        List<CommentResponse> responses = cheerTalks.stream()
                .map(comment -> new CommentResponse(
                        comment,
                        getOrderOfGameTeamId(comment.getGameTeamId(), gameTeamIds)
                ))
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

    private int getOrderOfGameTeamId(final Long gameTeamId, final List<Long> gameTeamIds) {
        return gameTeamIds.indexOf(gameTeamId) + 1;
    }
}
