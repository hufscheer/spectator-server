package com.sports.server.common.application;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CopyObjectRequest;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.sports.server.common.exception.CustomException;
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

}
