package com.superlawva.global.config;

import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.documentai.v1.DocumentProcessorServiceClient;
import com.google.cloud.documentai.v1.DocumentProcessorServiceSettings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

@Configuration
public class GcpConfig {

    @Value("${gcp.credentials.path}")
    private Resource credentialsPath;

    @Bean
    public GoogleCredentials googleCredentials() throws IOException {
        // Document AI를 포함한 모든 GCP 서비스에 대한 권한 범위(Scope)
        final String cloudPlatformScope = "https://www.googleapis.com/auth/cloud-platform";

        try {
            // 1. 배포 환경을 위한 ADC(Application Default Credentials) 시도
            GoogleCredentials credentials = GoogleCredentials.getApplicationDefault();
            System.out.println("[GCP] GoogleCredentials loaded successfully via ADC.");
            // 로드된 인증 정보에 명시적으로 권한 범위를 부여하여 반환
            return credentials.createScoped(cloudPlatformScope);
        } catch (IOException e) {
            // 2. ADC 실패 시, 로컬 환경을 위한 Classpath Resource 에서 로드
            System.out.println("[GCP] ADC failed. Falling back to classpath resource: " + credentialsPath.getFilename());
            if (credentialsPath == null || !credentialsPath.exists()) {
                throw new FileNotFoundException(
                        "GCP credential file not found at classpath:" + credentialsPath.getFilename() +
                        ", and ADC are not configured."
                );
            }
            try (InputStream inputStream = credentialsPath.getInputStream()) {
                // 파일에서 읽은 인증 정보에 명시적으로 권한 범위를 부여하여 반환
                GoogleCredentials credentials = GoogleCredentials.fromStream(inputStream)
                        .createScoped(cloudPlatformScope);
                System.out.println("[GCP] GoogleCredentials loaded and scoped from " + credentialsPath.getFilename());
                return credentials;
            }
        }
    }

    @Bean
    public DocumentProcessorServiceClient documentProcessorServiceClient(GoogleCredentials credentials) throws IOException {
        DocumentProcessorServiceSettings settings = DocumentProcessorServiceSettings.newBuilder()
                .setEndpoint("us-documentai.googleapis.com:443")
                .setCredentialsProvider(FixedCredentialsProvider.create(credentials))
                .build();

        return DocumentProcessorServiceClient.create(settings);
    }
}