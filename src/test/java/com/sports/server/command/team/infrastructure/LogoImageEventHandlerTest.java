package com.sports.server.command.team.infrastructure;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.sports.server.command.team.domain.LogoImageDeletedEvent;
import com.sports.server.common.application.S3Service;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.test.util.ReflectionTestUtils;

@DisplayName("로고 삭제 이벤트를 받으면")
class LogoImageEventHandlerTest {

    private static final String REPLACE_PREFIX = "https://images.hufscheer.com/";

    private final S3Service s3Service = mock(S3Service.class);
    private final LogoImageEventHandler handler = new LogoImageEventHandler(s3Service);

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(handler, "replacePrefix", REPLACE_PREFIX);
    }

    @Test
    void 이미지_도메인_주소에서_S3_키를_떼어_지운다() {
        handler.handle(new LogoImageDeletedEvent(REPLACE_PREFIX + "0b1c2d3e.png"));

        verify(s3Service).deleteFile("0b1c2d3e.png");
    }

    @ParameterizedTest(name = "[{0}]")
    @NullAndEmptySource
    @ValueSource(strings = {"https://example.com/logos/wildhorse.png", "0b1c2d3e.png"})
    void 우리_버킷_주소가_아니면_아무것도_지우지_않는다(String logoImageUrl) {
        handler.handle(new LogoImageDeletedEvent(logoImageUrl));

        verify(s3Service, never()).deleteFile(anyString());
    }
}
