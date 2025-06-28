package com.superlawva.global.config;

import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.documentai.v1.DocumentProcessorServiceClient;
import com.google.cloud.documentai.v1.DocumentProcessorServiceSettings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;

@Configuration
public class GcpConfig {

    @Value("${gcp.project-id}")
    private String projectId;

    @Value("${gcp.credentials.path}")
    private Resource credentialsPath;

    @Bean
    public GoogleCredentials googleCredentials() throws IOException {
        System.out.println("[GCP] Loading credentials from: " + credentialsPath.getURI());
        try (InputStream inputStream = credentialsPath.getInputStream()) {
            GoogleCredentials credentials = GoogleCredentials.fromStream(inputStream);
            System.out.println("[GCP] GoogleCredentials loaded successfully from " + credentialsPath.getFilename());
            return credentials;
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