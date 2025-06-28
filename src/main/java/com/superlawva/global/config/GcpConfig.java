package com.superlawva.global.config;

import org.springframework.context.annotation.Configuration;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.documentai.v1.DocumentProcessorServiceClient;
import com.google.cloud.documentai.v1.DocumentProcessorServiceSettings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class GcpConfig {

    @Value("${gcp.project-id}")
    private String projectId;

    @Bean
    public GoogleCredentials googleCredentials() throws IOException {
        // ADC를 사용하여 환경 변수에서 인증 정보를 자동으로 로드합니다.
        GoogleCredentials credentials = GoogleCredentials.getApplicationDefault();
        System.out.println("[GCP] GoogleCredentials loaded successfully via Application Default Credentials for project: " + projectId);
        return credentials;
    }

    @Bean
    public DocumentProcessorServiceClient documentProcessorServiceClient(GoogleCredentials credentials) throws IOException {
        DocumentProcessorServiceSettings settings = DocumentProcessorServiceSettings.newBuilder()
                .setCredentialsProvider(FixedCredentialsProvider.create(credentials))
                .build();

        return DocumentProcessorServiceClient.create(settings);
    }
}