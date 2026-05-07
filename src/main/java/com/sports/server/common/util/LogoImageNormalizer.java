package com.sports.server.common.util;

import com.sports.server.common.exception.CustomException;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.imageio.ImageIO;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.http.HttpStatus;

public final class LogoImageNormalizer {

    public static final int CANVAS_SIZE = 512;
    public static final int MAX_DIMENSION = 4096;
    public static final String OUTPUT_FORMAT = "png";
    public static final String OUTPUT_CONTENT_TYPE = "image/png";

    private static final Color BACKGROUND = Color.WHITE;

    private LogoImageNormalizer() {
    }

    public static byte[] normalize(byte[] source) {
        BufferedImage decoded = decode(source);
        if (decoded.getWidth() > MAX_DIMENSION || decoded.getHeight() > MAX_DIMENSION) {
            throw new CustomException(HttpStatus.PAYLOAD_TOO_LARGE, "이미지 해상도가 허용 한도(4096×4096)를 초과합니다.");
        }
        BufferedImage resized = resizeContain(decoded);
        BufferedImage centered = centerOnCanvas(resized);
        return encodePng(centered);
    }

    private static BufferedImage decode(byte[] source) {
        try {
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(source));
            if (image == null) {
                throw new CustomException(HttpStatus.BAD_REQUEST, "지원하지 않는 이미지 형식입니다.");
            }
            return image;
        } catch (IOException e) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "이미지 디코딩에 실패했습니다.");
        }
    }

    private static BufferedImage resizeContain(BufferedImage source) {
        try {
            return Thumbnails.of(source)
                    .size(CANVAS_SIZE, CANVAS_SIZE)
                    .keepAspectRatio(true)
                    .asBufferedImage();
        } catch (IOException e) {
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "이미지 리사이즈에 실패했습니다.");
        }
    }

    private static BufferedImage centerOnCanvas(BufferedImage resized) {
        BufferedImage canvas = new BufferedImage(CANVAS_SIZE, CANVAS_SIZE, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = canvas.createGraphics();
        try {
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.setColor(BACKGROUND);
            g.fillRect(0, 0, CANVAS_SIZE, CANVAS_SIZE);
            int offsetX = (CANVAS_SIZE - resized.getWidth()) / 2;
            int offsetY = (CANVAS_SIZE - resized.getHeight()) / 2;
            g.drawImage(resized, offsetX, offsetY, null);
        } finally {
            g.dispose();
        }
        return canvas;
    }

    private static byte[] encodePng(BufferedImage image) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            ImageIO.write(image, OUTPUT_FORMAT, out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "이미지 인코딩에 실패했습니다.");
        }
    }
}
