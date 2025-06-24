package com.superlawva.domain.ocr3.service;

// import com.fasterxml.jackson.databind.ObjectMapper;
// import com.google.api.gax.core.FixedCredentialsProvider;
// import com.google.auth.oauth2.GoogleCredentials;
// import com.google.cloud.documentai.v1.*;
// import com.google.protobuf.ByteString;
import com.superlawva.domain.ocr3.dto.GeminiResponse;
import com.superlawva.domain.ocr3.dto.OcrResponse;
import com.superlawva.domain.ocr3.entity.ContractData;
// import com.superlawva.domain.ocr3.repository.ContractDataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
// @ConditionalOnProperty(name = "gcp.enabled", havingValue = "true")
public class OcrService {
    
    // MongoDB 관련 주석처리
    // private final ContractDataRepository contractDataRepository;
    // private final RestTemplate restTemplate;
    // private final ObjectMapper objectMapper;
    // private final GoogleCredentials googleCredentials;
    
    @Value("${gcp.project-id:dummy}")
    private String projectId;
    
    @Value("${gcp.location:dummy}")
    private String location;
    
    @Value("${gcp.processor-id:dummy}")
    private String processorId;
    
    @Value("${gemini.api-key:dummy}")
    private String geminiApiKey;
    
    @Value("${gemini.model-name:dummy}")
    private String geminiModelName;
    
    @Value("${gemini.api-url:dummy}")
    private String geminiApiUrl;
    
    // OCR 처리 메서드 - 로컬 개발용 더미 구현
    public OcrResponse processContract(MultipartFile file) throws Exception {
        log.warn("🚧 OCR 서비스가 비활성화되었습니다. 더미 응답을 반환합니다.");
        
        // 더미 ContractData 생성
        ContractData dummyContract = new ContractData();
        dummyContract.setId("dummy-" + System.currentTimeMillis());
        dummyContract.setContractType("월세");
        dummyContract.setUserId("dummy-user");
        dummyContract.setGenerated(false);
        dummyContract.setFileUrl("file://" + file.getOriginalFilename());
        dummyContract.setCreatedDate(LocalDateTime.now());
        dummyContract.setModifiedDate(LocalDateTime.now());
        
        return OcrResponse.builder()
                .contractData(dummyContract)
                .debugMode(true)
                .build();
    }
    
    // 사용자 ID를 포함한 계약서 처리 메서드 - 더미 구현
    public OcrResponse processContractWithUserId(MultipartFile file, String userId) throws Exception {
        log.warn("🚧 OCR 서비스가 비활성화되었습니다. 더미 응답을 반환합니다.");
        
        ContractData dummyContract = new ContractData();
        dummyContract.setId("dummy-" + System.currentTimeMillis());
        dummyContract.setContractType("전세");
        dummyContract.setUserId(userId);
        dummyContract.setGenerated(false);
        dummyContract.setFileUrl("file://" + file.getOriginalFilename());
        dummyContract.setCreatedDate(LocalDateTime.now());
        dummyContract.setModifiedDate(LocalDateTime.now());
        
        return OcrResponse.builder()
                .contractData(dummyContract)
                .debugMode(true)
                .build();
    }
    
    // MongoDB 관련 메서드들 주석처리
    public List<ContractData> getAllContracts() {
        log.warn("🚧 MongoDB가 비활성화되었습니다. 빈 목록을 반환합니다.");
        return new ArrayList<>();
    }
    
    public ContractData getContractById(String id) {
        log.warn("🚧 MongoDB가 비활성화되었습니다. null을 반환합니다.");
        return null;
    }
    
    public List<ContractData> getContractsByUserId(String userId) {
        log.warn("🚧 MongoDB가 비활성화되었습니다. 빈 목록을 반환합니다.");
        return new ArrayList<>();
    }
    
    /*
    // ===== 원본 구현 (운영환경용) =====
    
    private String extractTextFromImage(MultipartFile file) throws IOException {
        log.debug("Initializing Document AI client");
        
        DocumentProcessorServiceSettings settings = DocumentProcessorServiceSettings.newBuilder()
                .setEndpoint(String.format("%s-documentai.googleapis.com:443", location))
                .setCredentialsProvider(FixedCredentialsProvider.create(googleCredentials))
                .build();
        
        try (DocumentProcessorServiceClient client = DocumentProcessorServiceClient.create(settings)) {
            String name = String.format("projects/%s/locations/%s/processors/%s", 
                    projectId, location, processorId);
            
            ByteString content = ByteString.copyFrom(file.getBytes());
            
            RawDocument rawDocument = RawDocument.newBuilder()
                    .setContent(content)
                    .setMimeType(file.getContentType() != null ? file.getContentType() : "image/jpeg")
                    .build();
            
            ProcessRequest request = ProcessRequest.newBuilder()
                    .setName(name)
                    .setRawDocument(rawDocument)
                    .build();
            
            ProcessResponse result = client.processDocument(request);
            return result.getDocument().getText();
        }
    }
    
    private GeminiResponse analyzeTextWithGemini(String ocrText) throws Exception {
        // Gemini API 호출 구현...
    }
    
    private String buildGeminiPrompt(String ocrText) {
        // 프롬프트 빌드 구현...
    }
    
    private String getJsonSchemaExample() {
        // JSON 스키마 예시...
    }
    */
}