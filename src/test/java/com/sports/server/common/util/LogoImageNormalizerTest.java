package com.sports.server.common.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.sports.server.common.exception.CustomException;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class LogoImageNormalizerTest {

    @Test
    @DisplayName("와이드 이미지를 정규화하면 512x512 정사각형으로 변환된다")
    void normalize_widthRectangle_to512Square() throws Exception {
        byte[] wide = renderSolidImage(1024, 256, Color.RED, "png");

        byte[] result = LogoImageNormalizer.normalize(wide);

        BufferedImage decoded = decode(result);
        assertThat(decoded.getWidth()).isEqualTo(LogoImageNormalizer.CANVAS_SIZE);
        assertThat(decoded.getHeight()).isEqualTo(LogoImageNormalizer.CANVAS_SIZE);
    }

    @Test
    @DisplayName("와이드 이미지를 정규화하면 위/아래 빈 영역이 흰색으로 패딩된다")
    void normalize_widthRectangle_paddedWithWhite() throws Exception {
        byte[] wide = renderSolidImage(1024, 256, Color.RED, "png");

        byte[] result = LogoImageNormalizer.normalize(wide);

        BufferedImage decoded = decode(result);
        Color topLeft = new Color(decoded.getRGB(0, 0));
        Color bottomRight = new Color(decoded.getRGB(decoded.getWidth() - 1, decoded.getHeight() - 1));
        assertThat(topLeft).isEqualTo(Color.WHITE);
        assertThat(bottomRight).isEqualTo(Color.WHITE);
    }

    @Test
    @DisplayName("정사각형 이미지를 정규화하면 패딩 없이 512x512로 변환된다")
    void normalize_square_to512Square() throws Exception {
        byte[] square = renderSolidImage(300, 300, Color.BLUE, "png");

        byte[] result = LogoImageNormalizer.normalize(square);

        BufferedImage decoded = decode(result);
        assertThat(decoded.getWidth()).isEqualTo(LogoImageNormalizer.CANVAS_SIZE);
        assertThat(decoded.getHeight()).isEqualTo(LogoImageNormalizer.CANVAS_SIZE);
        Color center = new Color(decoded.getRGB(decoded.getWidth() / 2, decoded.getHeight() / 2));
        assertThat(center).isEqualTo(Color.BLUE);
    }

    @Test
    @DisplayName("JPEG 입력도 PNG로 정규화된다")
    void normalize_jpegInput_toPng() throws Exception {
        byte[] jpeg = renderSolidImage(400, 400, Color.GREEN, "jpg");

        byte[] result = LogoImageNormalizer.normalize(jpeg);

        BufferedImage decoded = decode(result);
        assertThat(decoded.getWidth()).isEqualTo(LogoImageNormalizer.CANVAS_SIZE);
        assertThat(decoded.getHeight()).isEqualTo(LogoImageNormalizer.CANVAS_SIZE);
    }

    @Test
    @DisplayName("디코딩 불가능한 입력은 CustomException 으로 변환된다")
    void normalize_invalidBytes_throws() {
        byte[] garbage = "not-an-image".getBytes();

        assertThatThrownBy(() -> LogoImageNormalizer.normalize(garbage))
                .isInstanceOf(CustomException.class);
    }

    @Test
    @DisplayName("해상도가 허용 한도를 초과하면 CustomException 이 발생한다")
    void normalize_oversizedDimension_throws() throws Exception {
        int overLimit = LogoImageNormalizer.MAX_DIMENSION + 1;
        byte[] oversized = renderSolidImage(overLimit, overLimit, Color.RED, "png");

        assertThatThrownBy(() -> LogoImageNormalizer.normalize(oversized))
                .isInstanceOf(CustomException.class);
    }

    private static byte[] renderSolidImage(int width, int height, Color color, String format) throws Exception {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        try {
            g.setColor(color);
            g.fillRect(0, 0, width, height);
        } finally {
            g.dispose();
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(image, format, out);
        return out.toByteArray();
    }

    private static BufferedImage decode(byte[] bytes) throws Exception {
        return ImageIO.read(new ByteArrayInputStream(bytes));
    }
}
