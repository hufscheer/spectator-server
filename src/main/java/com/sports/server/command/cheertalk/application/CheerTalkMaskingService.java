package com.sports.server.command.cheertalk.application;

import com.sports.server.command.cheertalk.dto.CheerTalkMaskedResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CheerTalkMaskingService {

    private final MaskingClient maskingClient;
    private final CheerTalkContentUpdater cheerTalkContentUpdater;

    public CheerTalkMaskedResponse maskingCheerTalk(String content, Long cheerTalkId) {
        String maskedResponse = maskingClient.mask(content);
        if (content.equals(maskedResponse)) {
            return new CheerTalkMaskedResponse(false, maskedResponse);
        }

        cheerTalkContentUpdater.updateContent(cheerTalkId, maskedResponse);
        return new CheerTalkMaskedResponse(true, maskedResponse);
    }
}
