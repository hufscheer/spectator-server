package com.sports.server.common.application;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PresignedUrlGenerator {

    private final AmazonS3 amazonS3;

    @Value("${amazon.aws.bucket}")
    private String bucketName;

    public String generatePresignedUrl(String extension) {
        String filePath = getFilePath(extension);
        return amazonS3.generatePresignedUrl(bucketName, filePath, getExpiredDate(), HttpMethod.PUT).toString();
    }

    private String getFilePath(String extension) {
        return UUID.randomUUID() + "." + extension;
    }

    private Date getExpiredDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.MINUTE, 50);
        return calendar.getTime();
    }

}
