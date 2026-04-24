package com.sports.server.command.cheertalk.application;

import com.sports.server.command.cheertalk.domain.CheerTalk;
import com.sports.server.command.cheertalk.dto.CheerTalkMaskedResponse;
import com.sports.server.common.application.EntityUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CheerTalkMaskingService {

    private final MaskingClient maskingClient;
    private final EntityUtils entityUtils;

    // TODO: transaction 내부에서 외부 API 호출하지 않도록 처리하기
    @Transactional
    public CheerTalkMaskedResponse maskingCheerTalk(String content, Long cheerTalkId) {
        String maskedResponse = maskingClient.mask(content);
        if (content.equals(maskedResponse)) {
            return new CheerTalkMaskedResponse(false, maskedResponse);
        }

        CheerTalk cheerTalk = entityUtils.getEntity(cheerTalkId, CheerTalk.class);
        cheerTalk.updateContent(maskedResponse);

        return new CheerTalkMaskedResponse(true, maskedResponse);
    }
}
