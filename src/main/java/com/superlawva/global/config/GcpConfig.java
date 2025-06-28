package com.superlawva.global.config;

import org.springframework.context.annotation.Configuration;

// TODO: GCP 의존성 추가 후 활성화
/*
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

    @Value("${gcp.project.id}")
    private String projectId;

    @Value("${gcp.credentials.file.path}")
    private String credentialsFilePath;

    @Value("${gcp.documentai.processor.id}")
    private String processorId;

    @Value("${gcp.documentai.location}")
    private String location;

    @Bean
    public GoogleCredentials googleCredentials() throws IOException {
        return GoogleCredentials.fromStream(
                getClass().getResourceAsStream(credentialsFilePath)
        );
    }

    @Bean
    public DocumentProcessorServiceClient documentProcessorServiceClient(GoogleCredentials credentials) throws IOException {
        DocumentProcessorServiceSettings settings = DocumentProcessorServiceSettings.newBuilder()
                .setCredentialsProvider(FixedCredentialsProvider.create(credentials))
                .build();

        return DocumentProcessorServiceClient.create(settings);
    }
}
*/