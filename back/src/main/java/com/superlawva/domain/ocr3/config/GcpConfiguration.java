// Separate file: GcpConfiguration.java
package com.superlawva.domain.ocr3.config;

import com.google.auth.oauth2.GoogleCredentials;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import javax.annotation.PostConstruct;
import java.io.IOException;

@Slf4j
//@Configuration  // 임시 비활성화 (Bean 충돌 방지)
public class GcpConfiguration {
    
    @Value("${gcp.credentials.location:classpath:service-account-key.json}")
    private Resource credentialsLocation;
    
    @PostConstruct
    public void init() {
        try {
            // Set up Google Application Default Credentials
            System.setProperty("GOOGLE_APPLICATION_CREDENTIALS", 
                    credentialsLocation.getFile().getAbsolutePath());
            log.info("Google Cloud credentials configured successfully");
        } catch (IOException e) {
            log.error("Failed to configure Google Cloud credentials", e);
        }
    }
    
    @Bean("ocr3GoogleCredentials")
    public GoogleCredentials ocr3GoogleCredentials() throws IOException {
        return GoogleCredentials.fromStream(credentialsLocation.getInputStream())
                .createScoped("https://www.googleapis.com/auth/cloud-platform");
    }
}