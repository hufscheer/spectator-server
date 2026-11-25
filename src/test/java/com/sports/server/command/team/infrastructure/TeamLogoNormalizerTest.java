package com.sports.server.command.team.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.sports.server.command.team.domain.TeamRepository;
import com.sports.server.common.application.S3Service;
import com.sports.server.common.exception.CustomException;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;

@DisplayName("팀 로고를 정규화할 때")
class TeamLogoNormalizerTest {

    private static final String ORIGIN_PREFIX = "https://bucket.s3.ap-northeast-2.amazonaws.com/";
    private static final String REPLACE_PREFIX = "https://images.hufscheer.com/";

    private final S3Service s3Service = mock(S3Service.class);
    private final TeamRepository teamRepository = mock(TeamRepository.class);
    private final TeamLogoNormalizer normalizer = new TeamLogoNormalizer(s3Service, teamRepository);

    @BeforeEach
    void setUp() throws Exception {
        ReflectionTestUtils.setField(normalizer, "originPrefix", ORIGIN_PREFIX);
        ReflectionTestUtils.setField(normalizer, "replacePrefix", REPLACE_PREFIX);
        ByteArrayOutputStream png = new ByteArrayOutputStream();
        ImageIO.write(new BufferedImage(20, 10, BufferedImage.TYPE_INT_RGB), "png", png);
        when(s3Service.download("0b1c.png")).thenReturn(png.toByteArray());
    }

    @Test
    void 새_키에_올리고_주소를_바꾼_뒤_원본을_치운다() {
        when(teamRepository.replaceLogoImageUrl(anyString(), anyString())).thenReturn(1);

        normalizer.normalize(ORIGIN_PREFIX + "0b1c.png");

        verify(s3Service).upload(eq("normalized/0b1c.webp"), any(byte[].class), eq("image/webp"));
        verify(teamRepository).replaceLogoImageUrl(REPLACE_PREFIX + "0b1c.png", REPLACE_PREFIX + "normalized/0b1c.webp");
        verify(s3Service).deleteFile("0b1c.png");
    }

    @Test
    void 그_사이_로고가_바뀌어_바꿀_팀이_없으면_원본을_지우지_않는다() {
        when(teamRepository.replaceLogoImageUrl(anyString(), anyString())).thenReturn(0);

        normalizer.normalize(REPLACE_PREFIX + "0b1c.png");

        verify(s3Service, never()).deleteFile(anyString());
    }

    @Test
    void 원본_삭제에_실패해도_예외를_던지지_않는다() {
        when(teamRepository.replaceLogoImageUrl(anyString(), anyString())).thenReturn(1);
        doThrow(new CustomException(HttpStatus.BAD_REQUEST, "이미지 파일 삭제에 실패했습니다."))
                .when(s3Service).deleteFile("0b1c.png");

        assertThatCode(() -> normalizer.normalize(REPLACE_PREFIX + "0b1c.png")).doesNotThrowAnyException();
    }

    @Test
    void 이미_정규화된_주소는_건드리지_않는다() {
        normalizer.normalize(REPLACE_PREFIX + "normalized/0b1c.webp");

        verifyNoInteractions(s3Service, teamRepository);
    }

    @Test
    void 우리_버킷_주소가_아니면_건드리지_않는다() {
        normalizer.normalize("https://example.com/logos/wildhorse.png");

        verifyNoInteractions(s3Service, teamRepository);
    }

    @Test
    void 미리보기는_쓰지_않고_크기만_돌려준다() {
        TeamLogoNormalizer.Preview preview = normalizer.preview(REPLACE_PREFIX + "0b1c.png");

        assertThat(preview.normalizedKey()).isEqualTo("normalized/0b1c.webp");
        assertThat(preview.normalizedBytes()).isPositive();
        verify(s3Service, never()).upload(anyString(), any(byte[].class), anyString());
        verifyNoInteractions(teamRepository);
    }
}
