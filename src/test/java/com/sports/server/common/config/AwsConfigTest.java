package com.sports.server.common.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class AwsConfigTest {

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  "})
    void 키가_없으면_인스턴스_역할을_쓰는_기본_체인으로_접근한다(String accessKeyId) {
        AWSCredentialsProvider provider = AwsConfig.credentialsProvider(accessKeyId, "");

        assertThat(provider).isSameAs(DefaultAWSCredentialsProviderChain.getInstance());
    }

    @Test
    void 키가_있으면_그_키로_접근한다() {
        AWSCredentialsProvider provider = AwsConfig.credentialsProvider("local-access", "local-secret");

        assertThat(provider).isInstanceOf(AWSStaticCredentialsProvider.class);
        AWSCredentials credentials = provider.getCredentials();
        assertThat(credentials.getAWSAccessKeyId()).isEqualTo("local-access");
        assertThat(credentials.getAWSSecretKey()).isEqualTo("local-secret");
    }
}
