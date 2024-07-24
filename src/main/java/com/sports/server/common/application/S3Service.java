package com.sports.server.common.application;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CopyObjectRequest;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.sports.server.common.exception.CustomException;
import java.util.Date;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.joda.time.LocalDateTime;
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

    public void rollbackFile(String key) {
        String backupKey = backupPrefix + key;

        // 기존의 파일이 삭제되었고, 백업 파일이 존재하는 경우
        if (!doesFileExist(key) && doesFileExist(backupKey)) {
            amazonS3.copyObject(new CopyObjectRequest(bucketName, backupKey, bucketName, key));
        }

    }


    private boolean doesFileExist(String key) {
        return amazonS3.doesObjectExist(bucketName, key);
    }

    private String getFilePath(String extension) {
        return UUID.randomUUID() + "." + extension;
    }

    private Date getExpiredDate() {
        LocalDateTime now = new LocalDateTime().plusMinutes(50);
        return now.toDate();
    }

}
