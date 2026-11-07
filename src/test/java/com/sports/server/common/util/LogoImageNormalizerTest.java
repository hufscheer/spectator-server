package com.sports.server.common.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.sports.server.common.exception.CustomException;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
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
        assertNear(decoded.getRGB(0, 0), Color.WHITE);
        assertNear(decoded.getRGB(decoded.getWidth() - 1, decoded.getHeight() - 1), Color.WHITE);
    }

    @Test
    @DisplayName("정사각형 이미지를 정규화하면 패딩 없이 512x512로 변환된다")
    void normalize_square_to512Square() throws Exception {
        byte[] square = renderSolidImage(300, 300, Color.BLUE, "png");

        byte[] result = LogoImageNormalizer.normalize(square);

        BufferedImage decoded = decode(result);
        assertThat(decoded.getWidth()).isEqualTo(LogoImageNormalizer.CANVAS_SIZE);
        assertThat(decoded.getHeight()).isEqualTo(LogoImageNormalizer.CANVAS_SIZE);
        assertNear(decoded.getRGB(decoded.getWidth() / 2, decoded.getHeight() / 2), Color.BLUE);
    }

    @Test
    @DisplayName("JPEG 입력도 WebP로 정규화된다")
    void normalize_jpegInput_toWebp() throws Exception {
        byte[] jpeg = renderSolidImage(400, 400, Color.GREEN, "jpg");

        byte[] result = LogoImageNormalizer.normalize(jpeg);

        BufferedImage decoded = decode(result);
        assertThat(decoded.getWidth()).isEqualTo(LogoImageNormalizer.CANVAS_SIZE);
        assertThat(decoded.getHeight()).isEqualTo(LogoImageNormalizer.CANVAS_SIZE);
    }

    @Test
    @DisplayName("출력은 WebP 다")
    void normalize_outputIsWebp() throws Exception {
        byte[] png = renderSolidImage(300, 200, Color.RED, "png");

        byte[] result = LogoImageNormalizer.normalize(png);

        assertThat(new String(result, 0, 4, StandardCharsets.US_ASCII)).isEqualTo("RIFF");
        assertThat(new String(result, 8, 4, StandardCharsets.US_ASCII)).isEqualTo("WEBP");
        assertThat(LogoImageNormalizer.OUTPUT_CONTENT_TYPE).isEqualTo("image/webp");
    }

    @Test
    @DisplayName("WebP 로 올린 로고도 읽어서 정규화한다")
    void normalize_webpInput() throws Exception {
        byte[] webp = LogoImageNormalizer.normalize(renderSolidImage(300, 300, Color.BLUE, "png"));

        byte[] result = LogoImageNormalizer.normalize(webp);

        BufferedImage decoded = decode(result);
        assertThat(decoded.getWidth()).isEqualTo(LogoImageNormalizer.CANVAS_SIZE);
        assertNear(decoded.getRGB(256, 256), Color.BLUE);
    }

    @Test
    @DisplayName("사진처럼 복잡한 512px 이미지도 PNG 보다 작게 나온다")
    void normalize_smallerThanPng() throws Exception {
        BufferedImage noisy = new BufferedImage(512, 512, BufferedImage.TYPE_INT_RGB);
        java.util.Random random = new java.util.Random(42);
        for (int y = 0; y < 512; y++) {
            for (int x = 0; x < 512; x++) {
                int base = (x + y) / 4;
                noisy.setRGB(x, y, new Color(base % 256, (base * 3 + random.nextInt(24)) % 256, 128).getRGB());
            }
        }
        ByteArrayOutputStream png = new ByteArrayOutputStream();
        ImageIO.write(noisy, "png", png);

        byte[] result = LogoImageNormalizer.normalize(png.toByteArray());

        assertThat(result.length).isLessThan(png.size());
    }

    @Test
    @DisplayName("정규화본 키는 원본 키에서 정해지고 normalized/ 아래에 .webp 로 간다")
    void normalizedKeyOf() {
        assertThat(LogoImageNormalizer.normalizedKeyOf("0b1c2d3e.png")).isEqualTo("normalized/0b1c2d3e.webp");
        assertThat(LogoImageNormalizer.normalizedKeyOf("0b1c2d3e.webp")).isEqualTo("normalized/0b1c2d3e.webp");
        assertThat(LogoImageNormalizer.normalizedKeyOf("teams/a.b/logo.jpg")).isEqualTo("normalized/teams/a.b/logo.webp");
        assertThat(LogoImageNormalizer.normalizedKeyOf("noext")).isEqualTo("normalized/noext.webp");
        assertThat(LogoImageNormalizer.isNormalizedKey("normalized/0b1c2d3e.webp")).isTrue();
        assertThat(LogoImageNormalizer.isNormalizedKey("0b1c2d3e.png")).isFalse();
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

    /** 손실 WebP 는 YUV 로 바꿔 저장해 단색도 한두 단계 어긋날 수 있다 */
    private static void assertNear(int rgb, Color expected) {
        Color actual = new Color(rgb);
        assertThat(Math.abs(actual.getRed() - expected.getRed())).as("red %s", actual).isLessThanOrEqualTo(6);
        assertThat(Math.abs(actual.getGreen() - expected.getGreen())).as("green %s", actual).isLessThanOrEqualTo(6);
        assertThat(Math.abs(actual.getBlue() - expected.getBlue())).as("blue %s", actual).isLessThanOrEqualTo(6);
    }

    private static BufferedImage decode(byte[] bytes) throws Exception {
        return ImageIO.read(new ByteArrayInputStream(bytes));
    }
}
