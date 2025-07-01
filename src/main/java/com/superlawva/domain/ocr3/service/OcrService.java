package com.superlawva.domain.ocr3.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.documentai.v1.DocumentProcessorServiceClient;
import com.google.cloud.documentai.v1.DocumentProcessorServiceSettings;
import com.google.cloud.documentai.v1.RawDocument;
import com.google.cloud.documentai.v1.ProcessRequest;
import com.google.cloud.documentai.v1.ProcessResponse;
import com.google.protobuf.ByteString;
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
import org.apache.tika.Tika;
import org.springframework.transaction.annotation.Transactional;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OcrService {

    private final ContractDataRepository contractDataRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final S3Service s3Service;
    private final GoogleCredentials googleCredentials;
    private final DocumentProcessorServiceClient documentProcessorServiceClient;
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

    @Transactional
    public OcrResponse processContract(MultipartFile file) throws Exception {
        log.info("Starting OCR processing for file: {}", file.getOriginalFilename());

        // 🟢 0. 파일 S3 업로드 (guest -> contracts)
        String s3Url = uploadEncryptedToS3(file, "guest", "contracts");

        // Step 1: Extract text using Document AI
        String extractedText = extractTextFromImage(file);
        log.info("Text extraction completed");

        // Step 2: Analyze text with Gemini
        long startTime = System.currentTimeMillis();
        GeminiResponse geminiResponse = analyzeTextWithGemini(extractedText);
        double generationTime = (System.currentTimeMillis() - startTime) / 1000.0;
        log.info("Gemini analysis completed in {} seconds", generationTime);

        // Step 3: Prepare contract data (MySQL)
        ContractData contractData = mapGeminiToContractData(geminiResponse.getContractData(), file, null, generationTime);
        contractData.setFileUrl(s3Url);
        ContractData savedContract = contractDataRepository.save(contractData);
        log.info("Contract saved with ID: {}", savedContract.getId());

        // Step 4: Return response
        return OcrResponse.builder()
                .contractData(savedContract)
                .debugMode(geminiResponse.isDebugMode())
                .build();
    }

    @Transactional
    public OcrResponse processContractWithUserId(MultipartFile file, String userId) throws Exception {
        log.info("Starting OCR processing for file: {} with userId: {}", file.getOriginalFilename(), userId);

        String s3Url = uploadEncryptedToS3(file, userId, "contracts");

        // Step 1: Extract text using Document AI
        String extractedText = extractTextFromImage(file);
        log.info("Text extraction completed");

        // Step 2: Analyze text with Gemini
        long startTime = System.currentTimeMillis();
        GeminiResponse geminiResponse = analyzeTextWithGemini(extractedText);
        double generationTime = (System.currentTimeMillis() - startTime) / 1000.0;
        log.info("Gemini analysis completed in {} seconds", generationTime);

        // Step 3: Prepare contract data with userId (MySQL)
        ContractData contractData = mapGeminiToContractData(geminiResponse.getContractData(), file, userId, generationTime);
        contractData.setFileUrl(s3Url);
        ContractData savedContract = contractDataRepository.save(contractData);
        log.info("Contract saved with ID: {} for userId: {}", savedContract.getId(), userId);

        // Step 4: Return response
        return OcrResponse.builder()
                .contractData(savedContract)
                .debugMode(geminiResponse.isDebugMode())
                .build();
    }

    /**
     * FOR 종혁햄: DB 저장 없이 OCR 및 분석 결과만 반환
     */
    public OcrResponse processContractWithoutSaving(MultipartFile file) throws Exception {
        log.info("Starting OCR processing without saving (for JH) for file: {}", file.getOriginalFilename());

        String s3Url = uploadEncryptedToS3(file, "temp-user", "temp");

        // Step 1: Extract text using Document AI
        String extractedText = extractTextFromImage(file);
        log.info("Text extraction completed");

        // Step 2: Analyze text with Gemini
        long startTime = System.currentTimeMillis();
        GeminiResponse geminiResponse = analyzeTextWithGemini(extractedText);
        double generationTime = (System.currentTimeMillis() - startTime) / 1000.0;
        log.info("Gemini analysis completed in {} seconds", generationTime);

        // Step 3: Map Gemini response to ContractData object (BUT DO NOT SAVE)
        ContractData contractData = mapGeminiToContractData(geminiResponse.getContractData(), file, "temp-user", generationTime);
        contractData.setFileUrl(s3Url);
        
        // Step 4: Return response without saving to DB
        return OcrResponse.builder()
                .contractData(contractData)
                .debugMode(geminiResponse.isDebugMode())
                .build();
    }

    /**
     * FOR 종혁햄: DB 저장 없이 OCR 및 분석 결과 원본 JSON만 반환
     */
    public String processContractWithoutSavingRawJson(MultipartFile file) throws Exception {
        log.info("Starting OCR processing without saving (for JH, raw JSON) for file: {}", file.getOriginalFilename());

        // 1. S3 업로드
        String fileUrl = uploadEncryptedToS3(file, "temp-user", "temp");

        // 2. Gemini 분석 결과 원본 JSON
        String prompt = buildGeminiPrompt(extractedTextFromImage(file));
        Map<String, Object> requestBody = new HashMap<>();
        List<Map<String, String>> partsList = new ArrayList<>();
        Map<String, String> part = new HashMap<>();
        part.put("text", prompt);
        partsList.add(part);
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
            // file_url 필드만 추가
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.databind.node.ObjectNode root = (com.fasterxml.jackson.databind.node.ObjectNode) mapper.readTree(jsonText);
            root.put("file_url", fileUrl);
            return mapper.writeValueAsString(root);
        } else {
            throw new RuntimeException("Failed to get valid response from Gemini API");
        }
    }

    // S3 업로드 메서드를 public으로 변경
    public String uploadEncryptedToS3(MultipartFile file, String userId, String baseDir) throws IOException {
        byte[] originalBytes = file.getBytes();
        String base64 = java.util.Base64.getEncoder().encodeToString(originalBytes);
        String encrypted = com.superlawva.global.security.util.AESUtil.encrypt(base64);
        byte[] encryptedBytes = encrypted.getBytes(java.nio.charset.StandardCharsets.UTF_8);

        String ext = "";
        String original = file.getOriginalFilename();
        if (original != null && original.lastIndexOf('.') != -1) {
            ext = original.substring(original.lastIndexOf('.'));
        }
        String contentType = file.getContentType() != null ? file.getContentType() : "application/octet-stream";

        return s3Service.uploadBytes(encryptedBytes, userId, baseDir, ext, contentType);
    }

    // S3 업로드 없이 AI 원본 JSON만 반환
    public String processContractWithoutSavingRawJsonNoUpload(MultipartFile file) throws Exception {
        log.info("Starting OCR processing without saving (for JH, raw JSON, no upload) for file: {}", file.getOriginalFilename());
        // Step 1: Extract text using Document AI
        String extractedText = extractTextFromImage(file);
        log.info("Text extraction completed");
        // Step 2: Analyze text with Gemini (원본 JSON 그대로 반환)
        String prompt = buildGeminiPrompt(extractedText);
        Map<String, Object> requestBody = new HashMap<>();
        List<Map<String, String>> partsList = new ArrayList<>();
        Map<String, String> part = new HashMap<>();
        part.put("text", prompt);
        partsList.add(part);
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
            return jsonText;
        } else {
            throw new RuntimeException("Failed to get valid response from Gemini API");
        }
    }

    private ContractData mapGeminiToContractData(ContractData geminiData, MultipartFile file, String userId, double generationTime) throws JsonProcessingException {
        ContractData contractData = new ContractData();
        contractData.setUserId(userId != null ? userId : geminiData.getUserId());
        contractData.setContractType(geminiData.getContractType());
        contractData.setDates(geminiData.getDates());
        contractData.setProperty(geminiData.getProperty());
        contractData.setPayment(geminiData.getPayment());
        contractData.setLessor(geminiData.getLessor());
        contractData.setLessee(geminiData.getLessee());
        contractData.setBroker1(geminiData.getBroker1());
        contractData.setBroker2(geminiData.getBroker2());
        contractData.setIsGenerated(false);
        contractData.setFileUrl("file://" + file.getOriginalFilename());
        contractData.setCreatedDate(LocalDateTime.now(ZoneOffset.UTC));
        contractData.setModifiedDate(LocalDateTime.now(ZoneOffset.UTC));

        // JSON 변환이 필요한 필드 처리
        contractData.setArticlesJson(objectMapper.writeValueAsString(geminiData.getArticlesJson() != null ? geminiData.getArticlesJson() : geminiData.getArticlesJson()));
        contractData.setAgreementsJson(objectMapper.writeValueAsString(geminiData.getAgreementsJson() != null ? geminiData.getAgreementsJson() : geminiData.getAgreementsJson()));
        contractData.setRecommendedAgreementsJson(objectMapper.writeValueAsString(geminiData.getRecommendedAgreementsJson()));
        contractData.setLegalBasisJson(objectMapper.writeValueAsString(geminiData.getLegalBasisJson()));
        contractData.setCaseBasisJson(objectMapper.writeValueAsString(geminiData.getCaseBasisJson()));
        contractData.setAnalysisMetadataJson(objectMapper.writeValueAsString(geminiData.getAnalysisMetadataJson()));

        // 전체 계약 JSON 저장 (DB not null 컬럼 대비)
        contractData.setContractJson(objectMapper.writeValueAsString(geminiData));

        // ContractMetadata 세팅
        ContractData.ContractMetadata metadata = new ContractData.ContractMetadata();
        metadata.setModel(String.format("doc-ai:%s + gemini:%s", this.processorId, this.geminiModelName));
        metadata.setGenerationTime(generationTime);
        metadata.setUserAgent(null);
        metadata.setVersion("v3.1.0");
        contractData.setContractMetadata(metadata);

        return contractData;
    }

    private String extractTextFromImage(MultipartFile file) throws IOException {
        log.debug("Initializing Document AI client");
        
        Tika tika = new Tika();
        String mimeType = tika.detect(file.getInputStream());
        
        // 지원하는 MIME 타입 목록
        List<String> supportedMimeTypes = List.of("image/jpeg", "image/png", "image/tiff", "application/pdf");

        if (!supportedMimeTypes.contains(mimeType)) {
            log.error("Unsupported MIME type: {} for file: {}", mimeType, file.getOriginalFilename());
            throw new IllegalArgumentException("지원하지 않는 파일 형식입니다: " + mimeType);
        }

        log.info("Detected MIME type: {}", mimeType);

        String name = String.format("projects/%s/locations/%s/processors/%s", projectId, location, processorId);
        
        ByteString content = ByteString.copyFrom(file.getBytes());
        
        RawDocument rawDocument = RawDocument.newBuilder()
                .setContent(content)
                .setMimeType(mimeType) // Tika로 감지한 정확한 MIME 타입 사용
                .build();
        
        ProcessRequest request = ProcessRequest.newBuilder()
                .setName(name)
                .setRawDocument(rawDocument)
                .build();
        ProcessResponse result = documentProcessorServiceClient.processDocument(request);
        return result.getDocument().getText();
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
            "articles": [ "제2조 (존속기간) 임대인은 계약기간 내 임차인에게 해당 주택을 사용케 한다.", "제4조 (계약의 해지) 임차인이 3기의 차임액에 달하도록 연체하거나 제 3조를 위반하였을 때 임대인은 즉시 본 계약을 해지할 수 있다.", "제6조 (계약의 해제) 임차인이 임대인에게 중도금(중도금이 없을 때는 잔금)을 지불하기 전까지, 임대인은 계약금의 배액을 상환하고, 임차인은 계약금을 포기하고 이 계약을 해제할 수 있다." ],
            "agreements": [ "임차인은 반려동물 사육 시 손해 발생에 대한 책임을 진다.", "임차인의 고의 또는 과실로 인한 손상을 제외한 자연적 손상은 원상복구 의무를 면제한다.", "본 계약서에 명시되지 않은 사항은 주택임대차보호법 등 관련 법령에 따른다." ],
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

    // ==================== Helper Methods ====================
    private String extractedTextFromImage(MultipartFile file) throws Exception {
        return extractTextFromImage(file);
    }
}