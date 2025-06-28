package com.superlawva.domain.ocr3.service;

import com.fasterxml.jackson.databind.ObjectMapper;
// TODO: GCP Document AI 의존성 추가 후 활성화
/*
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.documentai.v1.*;
import com.google.protobuf.ByteString;
*/
import com.superlawva.domain.ocr3.dto.GeminiResponse;
import com.superlawva.domain.ocr3.dto.OcrResponse;
import com.superlawva.domain.ocr3.entity.ContractData;
import com.superlawva.domain.ocr3.repository.ContractDataRepository;
import com.superlawva.global.service.S3Service;
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
    private final S3Service s3Service;
    // TODO: GCP Document AI 의존성 추가 후 활성화
    // private final GoogleCredentials googleCredentials;

    // TODO: GCP Document AI 의존성 추가 후 활성화
    /*
    @Value("${gcp.project-id}")
    private String projectId;

    @Value("${gcp.location}")
    private String location;

    @Value("${gcp.processor-id}")
    private String processorId;
    */

    @Value("${gemini.api-key}")
    private String geminiApiKey;

    @Value("${gemini.model-name}")
    private String geminiModelName;

    @Value("${gemini.api-url}")
    private String geminiApiUrl;

    public OcrResponse processContract(MultipartFile file) throws Exception {
        log.info("Starting OCR processing for file: {}", file.getOriginalFilename());

        // Step 1: Upload image to S3
        String s3ImageUrl = s3Service.uploadImage(file, "anonymous");
        log.info("Image uploaded to S3: {}", s3ImageUrl);

        // TODO: GCP Document AI 의존성 추가 후 활성화
        // Step 2: Extract text using Document AI
        // String extractedText = extractTextFromImage(file);
        String extractedText = "임시 OCR 텍스트"; // 임시 구현
        log.info("Text extraction completed");

        // Step 3: Analyze text with Gemini
        long startTime = System.currentTimeMillis();
        GeminiResponse geminiResponse = analyzeTextWithGemini(extractedText);
        double generationTime = (System.currentTimeMillis() - startTime) / 1000.0;
        log.info("Gemini analysis completed in {} seconds", generationTime);

        // Step 4: Prepare contract data
        ContractData contractData = geminiResponse.getContractData();
        contractData.setUserId(null); // Will be set based on authenticated user
        contractData.setIsGenerated(false);
        contractData.setFileUrl(s3ImageUrl); // S3 URL 저장
        contractData.setCreatedDate(LocalDateTime.now(ZoneOffset.UTC));
        contractData.setModifiedDate(LocalDateTime.now(ZoneOffset.UTC));

        // Set metadata
        ContractData.ContractMetadata metadata = new ContractData.ContractMetadata();
        metadata.setModel(String.format("doc-ai:%s + gemini:%s", "temp-processor", geminiModelName));
        metadata.setGenerationTime(generationTime);
        metadata.setUserAgent(null);
        metadata.setVersion("v3.1.0");
        contractData.setContractMetadata(metadata);

        // Step 5: Save to database
        ContractData savedContract = contractDataRepository.save(contractData);
        log.info("Contract saved with ID: {}", savedContract.getId());

        // Step 6: Return response
        return OcrResponse.builder()
                .contractData(savedContract)
                .debugMode(geminiResponse.isDebugMode())
                .build();
    }

    // 🟢 사용자 ID를 포함한 계약서 처리 메서드 추가
    public OcrResponse processContractWithUserId(MultipartFile file, String userId) throws Exception {
        log.info("Starting OCR processing for file: {} with userId: {}", file.getOriginalFilename(), userId);

        // Step 1: Upload image to S3
        String s3ImageUrl = s3Service.uploadImage(file, userId);
        log.info("Image uploaded to S3: {}", s3ImageUrl);

        // Step 2: Extract text using OCR (임시 구현)
        String extractedText = extractTextFromImage(file);
        log.info("Text extraction completed - Length: {}", extractedText.length());

        // Step 3: Analyze text with Gemini
        long startTime = System.currentTimeMillis();
        GeminiResponse geminiResponse = analyzeTextWithGemini(extractedText);
        double generationTime = (System.currentTimeMillis() - startTime) / 1000.0;
        log.info("Gemini analysis completed in {} seconds", generationTime);

        // Step 4: Prepare contract data with userId
        ContractData contractData = geminiResponse.getContractData();
        contractData.setUserId(userId);  // 🔗 MySQL user.id 설정
        contractData.setIsGenerated(false);
        contractData.setFileUrl(s3ImageUrl); // S3 URL 저장
        contractData.setCreatedDate(LocalDateTime.now(ZoneOffset.UTC));
        contractData.setModifiedDate(LocalDateTime.now(ZoneOffset.UTC));

        // Set metadata
        ContractData.ContractMetadata metadata = new ContractData.ContractMetadata();
        metadata.setModel(String.format("ocr:%s + gemini:%s", "temp-ocr", geminiModelName));
        metadata.setGenerationTime(generationTime);
        metadata.setUserAgent(null);
        metadata.setVersion("v3.1.0");
        contractData.setContractMetadata(metadata);

        // Step 5: Save to database with userId
        ContractData savedContract = contractDataRepository.save(contractData);
        log.info("Contract saved with ID: {} for userId: {}", savedContract.getId(), userId);

        // Step 6: Return response
        return OcrResponse.builder()
                .contractData(savedContract)
                .debugMode(geminiResponse.isDebugMode())
                .build();
    }

    // 🟢 임시 OCR 텍스트 추출 구현 (실제 OCR 엔진 대체)
    private String extractTextFromImage(MultipartFile file) throws IOException {
        log.info("Extracting text from image: {} ({} bytes)", file.getOriginalFilename(), file.getSize());
        
        // 파일 확장자 확인
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new IllegalArgumentException("파일명이 없습니다.");
        }
        
        String fileExtension = originalFilename.toLowerCase();
        
        // PDF 파일인 경우
        if (fileExtension.endsWith(".pdf")) {
            return extractTextFromPdf(file);
        }
        // 이미지 파일인 경우
        else if (fileExtension.endsWith(".jpg") || fileExtension.endsWith(".jpeg") || 
                 fileExtension.endsWith(".png") || fileExtension.endsWith(".gif")) {
            return extractTextFromImageFile(file);
        }
        else {
            throw new IllegalArgumentException("지원하지 않는 파일 형식입니다: " + fileExtension);
        }
    }
    
    // 🟢 PDF에서 텍스트 추출 (간단한 구현)
    private String extractTextFromPdf(MultipartFile file) throws IOException {
        log.info("PDF 텍스트 추출 시작");
        
        // 실제 구현에서는 PDF 텍스트 추출 라이브러리 사용
        // 예: Apache PDFBox, iText 등
        
        // 임시로 샘플 계약서 텍스트 반환
        return """
        임대차계약서
        
        제1조 (계약의 목적)
        임대인은 임차인에게 서울시 강남구 테헤란로 123번지에 위치한 오피스텔을 임대하고, 
        임차인은 이를 임대받아 주거용으로 사용한다.
        
        제2조 (계약기간)
        계약기간은 2025년 1월 1일부터 2027년 12월 31일까지로 한다.
        
        제3조 (보증금 및 월세)
        1. 보증금: 5천만원
        2. 월세: 150만원
        3. 월세 납부일: 매월 5일
        
        임대인: 김영희 (주민등록번호: 850212-2345678)
        임차인: 박민수 (주민등록번호: 920405-3456789)
        
        계약일: 2024년 12월 15일
        """;
    }
    
    // 🟢 이미지에서 텍스트 추출 (간단한 구현)
    private String extractTextFromImageFile(MultipartFile file) throws IOException {
        log.info("이미지 텍스트 추출 시작");
        
        // 실제 구현에서는 OCR 라이브러리 사용
        // 예: Tesseract, Google Cloud Vision API 등
        
        // 임시로 샘플 계약서 텍스트 반환
        return """
        전세계약서
        
        임대인: 이철수
        임차인: 최영희
        
        임대목적물: 서울시 서초구 반포대로 123번지
        임대기간: 2025년 3월 1일 ~ 2027년 2월 28일
        전세금: 8천만원
        
        특약사항:
        1. 반려동물 허용
        2. 주차공간 1대 제공
        3. 관리비 월 15만원 별도
        
        계약일: 2025년 1월 15일
        """;
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
        log.info("Retrieving all contracts from database");
        List<ContractData> contracts = contractDataRepository.findAll();
        log.info("Found {} contracts", contracts.size());
        return contracts;
    }

    // 🟢 특정 ID로 계약서 조회
    public ContractData getContractById(Long id) {
        log.info("Retrieving contract with ID: {}", id);
        return contractDataRepository.findById(id).orElse(null);
    }

    // 🟢 사용자별 계약서 조회
    public List<ContractData> getContractsByUserId(String userId) {
        log.info("Retrieving contracts for user: {}", userId);
        return contractDataRepository.findByUserId(userId);
    }


}