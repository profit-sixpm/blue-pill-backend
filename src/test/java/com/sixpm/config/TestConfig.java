package com.sixpm.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.client.WebClient;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.regions.providers.AwsRegionProvider;
import software.amazon.awssdk.services.s3.S3Client;

@TestConfiguration
public class TestConfig {

    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .baseUrl("http://localhost:8080")
                .build();
    }

    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }

    @Bean
    public AwsCredentialsProvider awsCredentialsProvider() {
        // 테스트 환경에서는 더미 credentials 사용
        return StaticCredentialsProvider.create(
                AwsBasicCredentials.create("test-access-key", "test-secret-key")
        );
    }

    @Bean
    public AwsRegionProvider awsRegionProvider() {
        // 테스트 환경에서는 고정된 리전 사용
        return () -> Region.AP_NORTHEAST_2;
    }

    @Bean
    public S3Client s3Client(AwsCredentialsProvider credentialsProvider, AwsRegionProvider regionProvider) {
        // 테스트 환경에서는 실제 S3 연결 없이 빈만 생성
        return S3Client.builder()
                .region(regionProvider.getRegion())
                .credentialsProvider(credentialsProvider)
                .build();
    }
}

