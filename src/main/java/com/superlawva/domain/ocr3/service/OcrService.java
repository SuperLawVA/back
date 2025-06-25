package com.superlawva.domain.ocr3.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.documentai.v1.*;
import com.google.protobuf.ByteString;
import com.superlawva.domain.ocr3.dto.GeminiResponse;
import com.superlawva.domain.ocr3.dto.OcrResponse;
import com.superlawva.domain.ocr3.entity.ContractData;
import com.superlawva.domain.ocr3.repository.ContractDataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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
public class OcrService {
    
    private final ContractDataRepository contractDataRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final GoogleCredentials googleCredentials;
    
    @Value("${gcp.project-id}")
    private String projectId;
    
    @Value("${gcp.location}")
    private String location;
    
    @Value("${gcp.processor-id}")
    private String processorId;
    
    @Value("${gemini.api-key}")
    private String geminiApiKey;
    
    @Value("${gemini.model-name}")
    private String geminiModelName;
    
    @Value("${gemini.api-url}")
    private String geminiApiUrl;
    
    public OcrResponse processContract(MultipartFile file) throws Exception {
        log.info("Starting OCR processing for file: {}", file.getOriginalFilename());
        
        // Step 1: Extract text using Document AI
        String extractedText = extractTextFromImage(file);
        log.info("Text extraction completed");
        
        // Step 2: Analyze text with Gemini
        long startTime = System.currentTimeMillis();
        GeminiResponse geminiResponse = analyzeTextWithGemini(extractedText);
        double generationTime = (System.currentTimeMillis() - startTime) / 1000.0;
        log.info("Gemini analysis completed in {} seconds", generationTime);
        
        // Step 3: Prepare contract data
        ContractData contractData = geminiResponse.getContractData();
        contractData.setUserId(null); // Will be set based on authenticated user
        contractData.setGenerated(false);
        contractData.setFileUrl("file://" + file.getOriginalFilename());
        contractData.setCreatedDate(LocalDateTime.now(ZoneOffset.UTC));
        contractData.setModifiedDate(LocalDateTime.now(ZoneOffset.UTC));
        
        // Set metadata
        ContractData.ContractMetadata metadata = new ContractData.ContractMetadata();
        metadata.setModel(String.format("doc-ai:%s + gemini:%s", processorId, geminiModelName));
        metadata.setGenerationTime(generationTime);
        metadata.setUserAgent(null);
        metadata.setVersion("v3.1.0");
        contractData.setContractMetadata(metadata);
        
        // Step 4: Save to MongoDB
        ContractData savedContract = contractDataRepository.save(contractData);
        log.info("Contract saved with ID: {}", savedContract.get_id());
        
        // Step 5: Return response
        return OcrResponse.builder()
                .contractData(savedContract)
                .debugMode(geminiResponse.isDebugMode())
                .build();
    }
    
    // 🟢 사용자 ID를 포함한 계약서 처리 메서드 추가
    public OcrResponse processContractWithUserId(MultipartFile file, String userId) throws Exception {
        log.info("Starting OCR processing for file: {} with userId: {}", file.getOriginalFilename(), userId);
        
        // Step 1: Extract text using Document AI
        String extractedText = extractTextFromImage(file);
        log.info("Text extraction completed");
        
        // Step 2: Analyze text with Gemini
        long startTime = System.currentTimeMillis();
        GeminiResponse geminiResponse = analyzeTextWithGemini(extractedText);
        double generationTime = (System.currentTimeMillis() - startTime) / 1000.0;
        log.info("Gemini analysis completed in {} seconds", generationTime);
        
        // Step 3: Prepare contract data with userId
        ContractData contractData = geminiResponse.getContractData();
        contractData.setUserId(userId);  // 🔗 MySQL user.id 설정
        contractData.setGenerated(false);
        contractData.setFileUrl("file://" + file.getOriginalFilename());
        contractData.setCreatedDate(LocalDateTime.now(ZoneOffset.UTC));
        contractData.setModifiedDate(LocalDateTime.now(ZoneOffset.UTC));
        
        // Set metadata
        ContractData.ContractMetadata metadata = new ContractData.ContractMetadata();
        metadata.setModel(String.format("doc-ai:%s + gemini:%s", processorId, geminiModelName));
        metadata.setGenerationTime(generationTime);
        metadata.setUserAgent(null);
        metadata.setVersion("v3.1.0");
        contractData.setContractMetadata(metadata);
        
        // Step 4: Save to MongoDB with userId
        ContractData savedContract = contractDataRepository.save(contractData);
        log.info("Contract saved with ID: {} for userId: {}", savedContract.get_id(), userId);
        
        // Step 5: Return response
        return OcrResponse.builder()
                .contractData(savedContract)
                .debugMode(geminiResponse.isDebugMode())
                .build();
    }
    
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
        log.debug("Preparing Gemini API request");
        
        String prompt = buildGeminiPrompt(ocrText);
        
        Map<String, Object> requestBody = new HashMap<>();
        
        // Build parts list
        List<Map<String, String>> partsList = new ArrayList<>();
        Map<String, String> part = new HashMap<>();
        part.put("text", prompt);
        partsList.add(part);
        
        // Build contents list
        List<Map<String, Object>> contentsList = new ArrayList<>();
        Map<String, Object> content = new HashMap<>();
        content.put("parts", partsList);
        contentsList.add(content);
        
        requestBody.put("contents", contentsList);
        
        Map<String, String> generationConfig = new HashMap<>();
        generationConfig.put("response_mime_type", "application/json");
        requestBody.put("generationConfig", generationConfig);
        
        String url = String.format("%s/v1beta/models/%s:generateContent?key=%s", 
                geminiApiUrl, geminiModelName, geminiApiKey);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
        
        ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
        
        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            Map<String, Object> responseBody = response.getBody();
            List<Map<String, Object>> candidates = (List<Map<String, Object>>) responseBody.get("candidates");
            Map<String, Object> candidate = candidates.get(0);
            Map<String, Object> candidateContent = (Map<String, Object>) candidate.get("content");
            List<Map<String, String>> parts = (List<Map<String, String>>) candidateContent.get("parts");
            String jsonText = parts.get(0).get("text");
            
            return objectMapper.readValue(jsonText, GeminiResponse.class);
        } else {
            throw new RuntimeException("Failed to get valid response from Gemini API");
        }
    }
    
    private String buildGeminiPrompt(String ocrText) {
        String schemaExample = getJsonSchemaExample();
        
        return String.format("""
        당신은 한국 부동산 계약서를 분석하여 JSON으로 변환하는 AI 전문가입니다.
        다음 OCR 텍스트를 분석하여, 아래에 명시된 상세한 JSON 구조에 맞춰 내용을 채워주세요.

        ### 매우 중요한 규칙 ###
        1.  **없는 정보는 반드시 `null`**: 텍스트에 명시적으로 존재하지 않는 정보는 **절대로** 추측하거나 만들어내지 말고, 반드시 `null` 값으로 채워야 합니다. 빈 문자열("")이나 기본값을 사용하지 마세요.
        2.  **완전한 문장**: `articles`와 `agreements` 항목에 문자열을 추가할 때, 문장이 중간에 끊기지 않도록 완성된 전체 문장을 추출해야 합니다.
        3.  **정확한 값 추출**: 텍스트에 있는 내용만 정확하게 추출합니다.
        4.  **숫자 형식**: 금액, 면적 등은 반드시 따옴표 없는 숫자(Number) 형식으로 변환하세요.
        5.  **완벽한 JSON 출력**: 최종 결과는 오직 JSON 객체만 반환해야 합니다. 설명이나 다른 텍스트 없이 순수한 JSON 형식이어야 합니다.

        ### 최종 출력 JSON 구조 및 예시 (이 구조를 반드시 따르세요) ###
        %s

        ---
        ### 분석할 계약서 OCR 텍스트 ###
        ```text
        %s
        ```
        ---

        이제, 위 규칙과 구조에 따라 OCR 텍스트를 분석하여 완벽한 JSON을 생성해주세요.
        """, schemaExample, ocrText);
    }
    
    private String getJsonSchemaExample() {
        return """
        {
          "contract_data": {
            "contract_type": "전세",
            "dates": { "contract_date": "2025-06-14", "start_date": "2025-07-01", "end_date": "2027-06-30" },
            "property": { "address": "서울시 성동구 성수동 101-12", "detail_address": "B동 802호 8층", "rent_section": "전체", "rent_area": "70%", "land": { "land_type": "대지", "land_right_rate": "100분의 35", "land_area": 150.2 }, "building": { "building_constructure": "철근콘크리트", "building_type": "아파트", "building_area": "99.23" } },
            "payment": { "deposit": 80000000, "deposit_kr": "팔천만원정", "down_payment": 20000000, "down_payment_kr": "이천만원정", "intermediate_payment": 30000000, "intermediate_payment_kr": "삼천만원정", "intermediate_payment_date": "2026년3월15일", "remaining_balance": 30000000, "remaining_balance_kr": "삼천만원정", "remaining_balance_date": "2026년6월30일", "monthly_rent": null, "monthly_rent_date": "5일", "payment_plan": null },
            "articles": [ "제2조 (존속기간) 임대인은 계약기간 내 임차인에게 해당 주택을 사용케 한다." ],
            "agreements": [ "임차인은 반려동물 사육 시 손해 발생에 대한 책임을 진다." ],
            "lessor": { "name": "이영희", "id_number": "850212-2345678", "address": "서울시 서초구", "detail_address": "반포동 77-5", "phone_number": "02-555-6666", "mobile_number": "010-5555-6666", "agent": { "name": "이영희" } },
            "lessee": { "name": "김민준", "id_number": "920405-3456789", "address": "서울시 노원구", "detail_address": "중계동 12-34", "phone_number": "02-777-8888", "mobile_number": "010-7777-8888", "agent": { "name": "김민준" } },
            "broker1": { "office": "성수부동산중개법인", "license_number": "123-45-67890", "address": "서울시 성동구", "representative": "박대표", "fao_broker": "최중개사" },
            "broker2": { "office": null, "license_number": null, "address": null, "representative": null, "fao_broker": null }
          },
          "debug_mode": false
        }
        """;
    }
    
    // 모든 계약서 조회
    public List<ContractData> getAllContracts() {
        log.info("Retrieving all contracts from MongoDB");
        List<ContractData> contracts = contractDataRepository.findAll();
        log.info("Found {} contracts", contracts.size());
        return contracts;
    }
    
    // 🟢 특정 ID로 계약서 조회
    public ContractData getContractById(String id) {
        log.info("Retrieving contract with ID: {}", id);
        return contractDataRepository.findById(id).orElse(null);
    }
    
    // 🟢 사용자별 계약서 조회
    public List<ContractData> getContractsByUserId(String userId) {
        log.info("Retrieving contracts for user: {}", userId);
        return contractDataRepository.findByUserId(userId);
    }
    

}