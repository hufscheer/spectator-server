package com.sports.server.common.application;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CopyObjectRequest;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.sports.server.common.exception.CustomException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
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

    private final String backupPrefix = "backup/";

    public String generatePresignedUrl(String extension) {
        String filePath = getFilePath(extension);
        return amazonS3.generatePresignedUrl(bucketName, filePath, getExpiredDate(), HttpMethod.PUT).toString();
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
