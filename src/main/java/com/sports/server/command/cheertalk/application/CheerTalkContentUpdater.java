package com.sports.server.command.cheertalk.application;

import com.sports.server.command.cheertalk.domain.CheerTalk;
import com.sports.server.common.application.EntityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class CheerTalkContentUpdater {

    private final EntityUtils entityUtils;

    @Transactional
    public void updateContent(Long cheerTalkId, String content) {
        CheerTalk cheerTalk = entityUtils.getEntity(cheerTalkId, CheerTalk.class);
        cheerTalk.updateContent(content);
    }
}
