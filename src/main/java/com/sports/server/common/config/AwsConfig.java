package com.sports.server.common.config;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AwsConfig {

    // 서버(dev·prod)는 키를 주지 않는다. 비어 있으면 기본 자격 증명 체인, 즉 EC2 인스턴스 역할로 S3 에 접근한다.
    // 고정 키가 설정 파일과 이미지에 남아 옛 AWS 계정이 정지됐다. 로컬·CI 는 application-local/ci.yml 의 값을 쓴다
    @Value("${amazon.aws.accessKey:}")
    private String accessKeyId;

    @Value("${amazon.aws.secretKey:}")
    private String accessKeySecret;

    @Value("${amazon.aws.region}")
    private String regionName;

    @Bean
    public AmazonS3 getAmazonS3Client() {
        return AmazonS3ClientBuilder
                .standard()
                .withCredentials(credentialsProvider(accessKeyId, accessKeySecret))
                .withRegion(regionName)
                .build();
    }

    static AWSCredentialsProvider credentialsProvider(String accessKeyId, String accessKeySecret) {
        if (accessKeyId == null || accessKeyId.isBlank()) {
            return DefaultAWSCredentialsProviderChain.getInstance();
        }
        return new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKeyId, accessKeySecret));
    }
}
