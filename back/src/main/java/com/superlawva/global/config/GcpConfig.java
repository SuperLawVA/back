package com.superlawva.global.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.util.ResourceUtils;

import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.documentai.v1.DocumentProcessorServiceClient;
import com.google.cloud.documentai.v1.DocumentProcessorServiceSettings;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class GcpConfig {

    @Value("${gcp.project-id}")
    private String projectId;

    @Value("${gcp.location}")
    private String location;

    @Value("${gcp.processor-id}")
    private String processorId;

    @Value("${gcp.credentials.path:#{null}}")
    private String credentialsPath;
//
//    public String getProjectId() {
//        return projectId;
//    }
//
//    public String getLocation() {
//        return location;
//    }
//
//    public String getProcessorId() {
//        return processorId;
//    }

    @Bean
    @Primary
    @ConditionalOnProperty(name = "gcp.enabled", havingValue = "true")
    public GoogleCredentials googleCredentials() throws IOException {
        log.info("GCP 인증 정보 로드 중...");
        GoogleCredentials credentials;
        
        if (credentialsPath != null && !credentialsPath.isEmpty()) {
            log.info("지정된 경로에서 GCP 인증 정보 파일 로드: {}", credentialsPath);
            try (InputStream in = new FileInputStream(ResourceUtils.getFile(credentialsPath))) {
                credentials = GoogleCredentials.fromStream(in);
            }
        } else {
            log.info("기본 Application Default Credentials 사용");
            credentials = GoogleCredentials.getApplicationDefault();
        }
        
        // Document AI API에 필요한 스코프 추가
        return credentials.createScoped("https://www.googleapis.com/auth/cloud-platform");
    }

    @Bean
    @ConditionalOnProperty(name = "gcp.enabled", havingValue = "true")
    public DocumentProcessorServiceClient documentProcessorServiceClient(GoogleCredentials credentials) throws IOException {
        try {
            // us-central1 -> us-documentai.googleapis.com
            String endpointRegion = location.startsWith("us") ? "us" : location;
            String endpoint = String.format("%s-documentai.googleapis.com:443", endpointRegion);
            log.info("Document AI 클라이언트 생성 중 - Endpoint: {}, Project: {}, Processor: {}", 
                    endpoint, projectId, processorId);
            
            DocumentProcessorServiceSettings settings = DocumentProcessorServiceSettings.newBuilder()
                    .setEndpoint(endpoint)
                    .setCredentialsProvider(FixedCredentialsProvider.create(credentials))
                    .build();
            
            DocumentProcessorServiceClient client = DocumentProcessorServiceClient.create(settings);
            log.info("Document AI 클라이언트 생성 성공");
            return client;
            
        } catch (Exception e) {
            log.error("Document AI 클라이언트 생성 실패: {}", e.getMessage(), e);
            throw new IOException("Document AI 클라이언트 생성 실패: " + e.getMessage(), e);
        }
    }
}