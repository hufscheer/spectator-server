package com.sports.server.common.application;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CopyObjectRequest;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.sports.server.common.exception.BadRequestException;
import com.sports.server.common.exception.CustomException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class S3Service {

    private final AmazonS3 amazonS3;

    @Value("${amazon.aws.bucket}")
    private String bucketName;

    private static final long MAX_DOWNLOAD_BYTES = 10L * 1024 * 1024;

    private final String backupPrefix = "backup/";

    /**
     * 사전 서명 URL 로 올릴 수 있는 형식과, 그 형식으로 서명에 묶을 Content-Type.
     *
     * <p>목록은 운영 버킷에 실제로 올라와 있는 형식이다(2026-09-23, 442건 전수). SVG 는 뺐다 —
     * 이미지이면서 스크립트를 담을 수 있어서, 이미지 도메인에서 직접 열면 그 도메인으로 스크립트가
     * 돈다. {@code LogoImageNormalizer} 도 SVG 는 못 읽어 정규화도 안 된다.
     */
    private static final Map<String, String> IMAGE_CONTENT_TYPES = Map.of(
            "png", "image/png",
            "jpg", "image/jpeg",
            "jpeg", "image/jpeg",
            "gif", "image/gif",
            "webp", "image/webp",
            "avif", "image/avif"
    );

    /**
     * 이미지 한 장을 올릴 사전 서명 PUT URL 을 만든다.
     *
     * <p>확장자만 막아서는 부족하다. 서명에 Content-Type 이 없으면 {@code uuid.png} 를
     * {@code text/html} 로 올릴 수 있고, S3 는 저장된 타입대로 내보내므로 브라우저가 HTML 로 그린다.
     * 그래서 확장자에서 정한 Content-Type 을 서명에 묶는다 — 클라이언트가 다른 타입으로 올리면
     * S3 가 서명 불일치로 거절한다.
     */
    public String generatePresignedUrl(String extension) {
        String normalized = normalizeExtension(extension);
        String contentType = IMAGE_CONTENT_TYPES.get(normalized);
        if (contentType == null) {
            throw new BadRequestException("이미지 파일만 올릴 수 있습니다. (png, jpg, jpeg, gif, webp, avif)");
        }

        GeneratePresignedUrlRequest request =
                new GeneratePresignedUrlRequest(bucketName, getFilePath(normalized), HttpMethod.PUT)
                        .withExpiration(getExpiredDate())
                        .withContentType(contentType);
        return amazonS3.generatePresignedUrl(request).toString();
    }

    /** 아이폰 사진은 {@code IMG_0001.JPG} 처럼 대문자로 온다. 운영 버킷에도 JPG·PNG 가 있다 */
    private String normalizeExtension(String extension) {
        if (extension == null) {
            return "";
        }
        return extension.strip().toLowerCase(Locale.ROOT);
    }

    public void deleteFile(String key) {
        try {
            String backupKey = backupPrefix + key;
            amazonS3.copyObject(new CopyObjectRequest(bucketName, key, bucketName, backupKey));

            amazonS3.deleteObject(new DeleteObjectRequest(bucketName, key));

        } catch (Exception e) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "이미지 파일 삭제에 실패했습니다.");
        }
    }

    private String getFilePath(String extension) {
        return UUID.randomUUID() + "." + extension;
    }

    private Date getExpiredDate() {
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Seoul")).plusMinutes(50);
        return Date.from(now.toInstant());
    }

    public void doesFileExist(String key) {
        try {
            amazonS3.doesObjectExist(bucketName, key);
        } catch (Exception e) {
            throw new CustomException(HttpStatus.NOT_FOUND, "S3에 해당 파일이 존재하지 않습니다.");
        }
    }

    public byte[] download(String key) {
        try {
            ObjectMetadata meta = amazonS3.getObjectMetadata(bucketName, key);
            if (meta.getContentLength() > MAX_DOWNLOAD_BYTES) {
                throw new CustomException(HttpStatus.PAYLOAD_TOO_LARGE, "이미지 파일이 허용 용량(10MB)을 초과합니다.");
            }
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            throw new CustomException(HttpStatus.NOT_FOUND, "S3에서 이미지를 다운로드할 수 없습니다.");
        }
        try (S3Object object = amazonS3.getObject(bucketName, key);
             InputStream content = object.getObjectContent();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            content.transferTo(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new CustomException(HttpStatus.NOT_FOUND, "S3에서 이미지를 다운로드할 수 없습니다.");
        }
    }

    public void upload(String key, byte[] bytes, String contentType) {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(contentType);
        metadata.setContentLength(bytes.length);
        try {
            amazonS3.putObject(new PutObjectRequest(bucketName, key, new ByteArrayInputStream(bytes), metadata));
        } catch (Exception e) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "이미지 파일 업로드에 실패했습니다.");
        }
    }

}
