package com.sports.server.common.util;

import com.luciad.imageio.webp.WebPImageReaderSpi;
import com.luciad.imageio.webp.WebPImageWriterSpi;
import com.sports.server.common.exception.CustomException;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.spi.IIORegistry;
import javax.imageio.stream.ImageOutputStream;
import javax.imageio.stream.MemoryCacheImageOutputStream;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.http.HttpStatus;

public final class LogoImageNormalizer {

    public static final int CANVAS_SIZE = 512;
    public static final int MAX_DIMENSION = 4096;
    public static final String OUTPUT_EXTENSION = "webp";
    public static final String OUTPUT_CONTENT_TYPE = "image/webp";

    /**
     * 정규화본을 두는 경로. 원본 키를 덮어쓰지 않고 이 아래 새 키에 쓴다.
     *
     * <p>CDN 이 {@code Cache-Control: immutable} 1년을 주므로(2026-11-07), 같은 키를 덮어쓰면 정규화 전에
     * 원본을 받은 브라우저는 그 원본을 1년 동안 쓴다. 출력 형식이나 크기를 바꿀 때는 같은 이유로
     * 경로도 바꾼다(예: {@code normalized-v2/}).
     */
    public static final String NORMALIZED_PREFIX = "normalized/";

    /** 로고는 단색 그래픽이 많아 손실 80 이면 512px 에서 눈에 띄는 차이가 없다. 실측 184건 22.4MB 가 3.1MB 로 준다 */
    private static final float WEBP_QUALITY = 0.8f;

    private static final Color BACKGROUND = Color.WHITE;

    static {
        // ImageIO 는 처음 쓰는 스레드의 클래스로더로 플러그인을 찾는다. 스프링 부트 jar 나 비동기 스레드에서
        // 못 찾을 수 있어 직접 등록한다. 읽기도 등록해 WebP 로 올린 로고도 정규화한다
        IIORegistry registry = IIORegistry.getDefaultInstance();
        registry.registerServiceProvider(new WebPImageReaderSpi());
        registry.registerServiceProvider(new WebPImageWriterSpi());
    }

    private LogoImageNormalizer() {
    }

    /** {@code 0b1c.png} 는 {@code normalized/0b1c.webp} 로 간다. 같은 원본은 늘 같은 키라 다시 돌려도 결과가 같다 */
    public static String normalizedKeyOf(String originalKey) {
        int slash = originalKey.lastIndexOf('/');
        int dot = originalKey.lastIndexOf('.');
        String stem = dot > slash ? originalKey.substring(0, dot) : originalKey;
        return NORMALIZED_PREFIX + stem + "." + OUTPUT_EXTENSION;
    }

    public static boolean isNormalizedKey(String key) {
        return key.startsWith(NORMALIZED_PREFIX);
    }

    public static byte[] normalize(byte[] source) {
        BufferedImage decoded = decode(source);
        if (decoded.getWidth() > MAX_DIMENSION || decoded.getHeight() > MAX_DIMENSION) {
            throw new CustomException(HttpStatus.CONTENT_TOO_LARGE, "이미지 해상도가 허용 한도(4096×4096)를 초과합니다.");
        }
        BufferedImage resized = resizeContain(decoded);
        BufferedImage centered = centerOnCanvas(resized);
        return encodeWebp(centered);
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

    private static byte[] encodeWebp(BufferedImage image) {
        ImageWriter writer = createWebpWriter();
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             ImageOutputStream ios = new MemoryCacheImageOutputStream(out)) {
            ImageWriteParam param = writer.getDefaultWriteParam();
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionType("Lossy");
            param.setCompressionQuality(WEBP_QUALITY);
            writer.setOutput(ios);
            writer.write(null, new IIOImage(image, null, null), param);
            ios.flush();
            return out.toByteArray();
        } catch (IOException | RuntimeException | UnsatisfiedLinkError e) {
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "이미지 인코딩에 실패했습니다.");
        } finally {
            writer.dispose();
        }
    }

    private static ImageWriter createWebpWriter() {
        try {
            return new WebPImageWriterSpi().createWriterInstance();
        } catch (IOException | RuntimeException | UnsatisfiedLinkError e) {
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "이미지 인코딩에 실패했습니다.");
        }
    }
}
