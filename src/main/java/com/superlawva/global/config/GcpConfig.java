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
        try {
            // 1. 배포 환경을 위한 ADC(Application Default Credentials) 시도
            // GOOGLE_APPLICATION_CREDENTIALS 환경 변수를 자동으로 찾음
            GoogleCredentials credentials = GoogleCredentials.getApplicationDefault();
            System.out.println("[GCP] GoogleCredentials loaded successfully via Application Default Credentials.");
            return credentials;
        } catch (IOException e) {
            // 2. ADC 실패 시, 로컬 환경을 위한 Classpath Resource 에서 로드
            System.out.println("[GCP] Application Default Credentials failed. Falling back to classpath resource: " + credentialsPath.getFilename());
            if (credentialsPath == null || !credentialsPath.exists()) {
                throw new FileNotFoundException(
                        "GCP credential file not found at classpath:" + credentialsPath.getFilename() +
                        ", and Application Default Credentials are not configured. Please check your local setup."
                );
            }
            try (InputStream inputStream = credentialsPath.getInputStream()) {
                GoogleCredentials credentials = GoogleCredentials.fromStream(inputStream);
                System.out.println("[GCP] GoogleCredentials loaded successfully from " + credentialsPath.getFilename());
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