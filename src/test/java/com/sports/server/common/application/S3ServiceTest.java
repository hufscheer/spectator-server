package com.sports.server.common.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.amazonaws.HttpMethod;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.sports.server.common.exception.BadRequestException;
import java.net.URL;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

@DisplayName("이미지 업로드용 사전 서명 URL 은")
class S3ServiceTest {

    private static final String BUCKET = "test-bucket";

    private S3Service serviceWith(AmazonS3 amazonS3) {
        S3Service service = new S3Service(amazonS3);
        ReflectionTestUtils.setField(service, "bucketName", BUCKET);
        return service;
    }

    private GeneratePresignedUrlRequest capturedRequest(String extension) throws Exception {
        AmazonS3 amazonS3 = mock(AmazonS3.class);
        when(amazonS3.generatePresignedUrl(any(GeneratePresignedUrlRequest.class)))
                .thenReturn(new URL("https://" + BUCKET + ".s3.amazonaws.com/x"));

        serviceWith(amazonS3).generatePresignedUrl(extension);

        ArgumentCaptor<GeneratePresignedUrlRequest> captor =
                ArgumentCaptor.forClass(GeneratePresignedUrlRequest.class);
        verify(amazonS3).generatePresignedUrl(captor.capture());
        return captor.getValue();
    }

    @ParameterizedTest(name = "{0} → {1}")
    @CsvSource({
            "png, image/png",
            "jpg, image/jpeg",
            "jpeg, image/jpeg",
            "gif, image/gif",
            "webp, image/webp",
            "avif, image/avif",
            // 아이폰 사진은 IMG_0001.JPG 처럼 대문자로 온다. 운영 버킷에도 JPG·PNG 가 있다
            "JPG, image/jpeg",
            "PNG, image/png",
            "' jpeg ', image/jpeg",
    })
    void 이미지_형식이면_그_형식의_Content_Type을_서명에_묶는다(String extension, String contentType)
            throws Exception {
        GeneratePresignedUrlRequest request = capturedRequest(extension);

        assertThat(request.getMethod()).isEqualTo(HttpMethod.PUT);
        assertThat(request.getBucketName()).isEqualTo(BUCKET);
        assertThat(request.getContentType()).isEqualTo(contentType);
    }

    @Test
    void 파일_이름의_확장자는_소문자로_맞춘다() throws Exception {
        GeneratePresignedUrlRequest request = capturedRequest("JPG");

        assertThat(request.getKey()).endsWith(".jpg");
    }

    @ParameterizedTest
    @ValueSource(strings = {"html", "htm", "HTML", "svg", "js", "exe", "txt", "heic", "png.html", "  "})
    @NullAndEmptySource
    void 이미지가_아니면_막는다(String extension) {
        S3Service service = serviceWith(mock(AmazonS3.class));

        assertThatThrownBy(() -> service.generatePresignedUrl(extension))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("이미지 파일만");
    }

    /*
     * 위 테스트는 "Content-Type 을 요청에 담았다" 까지만 본다. 그게 실제로 서명에 들어가야
     * 다른 타입으로 올린 PUT 을 S3 가 거절한다. 실제 SDK 로 URL 을 만들어 서명 헤더를 확인한다.
     * URL 생성은 로컬 계산이라 네트워크를 타지 않고, 키는 가짜다.
     */
    @Test
    void 발급한_URL은_Content_Type을_서명_헤더에_넣는다() {
        AmazonS3 realClient = AmazonS3ClientBuilder.standard()
                .withRegion("ap-northeast-2")
                .withCredentials(new AWSStaticCredentialsProvider(
                        new BasicAWSCredentials("AKIAFAKEFAKEFAKEFAKE", "fake-secret")))
                .build();

        String url = serviceWith(realClient).generatePresignedUrl("png");

        assertThat(url).contains("X-Amz-Algorithm=AWS4-HMAC-SHA256");
        assertThat(url).containsPattern("X-Amz-SignedHeaders=[^&]*content-type");
    }
}
