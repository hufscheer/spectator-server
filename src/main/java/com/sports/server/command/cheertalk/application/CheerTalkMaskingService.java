package com.sports.server.command.cheertalk.application;

import com.sports.server.command.cheertalk.dto.CheerTalkMaskedResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CheerTalkMaskingService {

    private final MaskingClient maskingClient;
    private final CheerTalkBotFilterService cheerTalkBotFilterService;

    public CheerTalkMaskedResponse maskingCheerTalk(String content, Long cheerTalkId, Long gameId) {
        long startTime = System.currentTimeMillis();
        String maskedResponse = maskingClient.mask(content);
        int latencyMs = (int) (System.currentTimeMillis() - startTime);

        if (content.equals(maskedResponse)) {
            return new CheerTalkMaskedResponse(false, maskedResponse, false);
        }

        cheerTalkBotFilterService.blockMasked(
                cheerTalkId, gameId, content, maskedResponse, maskingClient.botType(), latencyMs);
        return new CheerTalkMaskedResponse(true, maskedResponse, true);
    }
}
