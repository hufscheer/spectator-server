package com.sports.server.command.cheertalk.application;

import static com.sports.server.command.cheertalk.exception.CheerTalkErrorMessages.CHEER_TALK_CONTAINS_BAD_WORD;

import com.sports.server.command.cheertalk.domain.CheerTalk;
import com.sports.server.command.cheertalk.domain.CheerTalkRepository;
import com.sports.server.command.cheertalk.domain.LanguageFilter;
import com.sports.server.command.cheertalk.dto.CheerTalkRequest;
import com.sports.server.common.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class CheerTalkService {

    private final CheerTalkRepository cheerTalkRepository;
    private final LanguageFilter languageFilter;

    public void register(final CheerTalkRequest cheerTalkRequest) {
        validateContent(cheerTalkRequest.content());
        CheerTalk cheerTalk = new CheerTalk(cheerTalkRequest.content(), cheerTalkRequest.gameTeamId());
        cheerTalkRepository.save(cheerTalk);
    }

    private void validateContent(final String content) {
        if (languageFilter.containsBadWord(content)) {
            throw new CustomException(HttpStatus.BAD_REQUEST, CHEER_TALK_CONTAINS_BAD_WORD);
        }
    }
}
