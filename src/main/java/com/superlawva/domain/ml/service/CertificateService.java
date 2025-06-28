package com.superlawva.domain.ml.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.superlawva.domain.ml.client.MLApiClient;
import com.superlawva.domain.ml.dto.CertificateCreateRequest;
import com.superlawva.domain.ml.dto.CertificateResponse;
import com.superlawva.domain.ml.dto.CertificateUpdateRequest;
import com.superlawva.domain.ml.entity.CertificateEntity;
import com.superlawva.domain.ml.repository.CertificateRepository;
import com.superlawva.domain.ocr3.entity.ContractData;
import com.superlawva.domain.ocr3.repository.ContractDataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CertificateService {

    private final CertificateRepository certificateRepository;
    private final ContractDataRepository contractDataRepository;
    private final MLApiClient mlApiClient;
    private final ObjectMapper objectMapper;

    /**
     * CREATE - 내용증명서 생성
     */
    @Transactional
    public CertificateResponse createCertificate(CertificateCreateRequest request) {
        log.info("📝 내용증명서 생성 요청 - Contract ID: {}", request.getContractId());

        try {
            ContractData contract = contractDataRepository.findById(Long.valueOf(request.getContractId()))
                    .orElseThrow(() -> new RuntimeException("계약서를 찾을 수 없습니다: " + request.getContractId()));

            if (contract.getUserId() != null) {
                request.setUserId(contract.getUserId());
            } else {
                log.warn("ContractData에 UserId가 없습니다. Contract ID: {}", request.getContractId());
                throw new IllegalStateException("계약서에 사용자 정보가 없습니다.");
            }

            log.info("📋 계약서 조회 완료 - User ID: {}, Contract Type: {}",
                    contract.getUserId(), contract.getContractType());

            Map<String, Object> mlRequestData = buildCertificateRequest(contract, request);
            Map<String, Object> mlResponse = mlApiClient.generateProofDocument(mlRequestData);
            CertificateEntity savedCertificate = saveCertificateResult(contract, mlResponse, request);

            log.info("✅ 내용증명서 생성 완료 - Certificate ID: {}", savedCertificate.getId());
            return convertToResponse(savedCertificate);

        } catch (Exception e) {
            log.error("❌ 내용증명서 생성 실패 - Contract ID: {}, Error: {}",
                    request.getContractId(), e.getMessage());
            throw new RuntimeException("내용증명서 생성 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * READ - 내용증명서 개별 조회
     */
    public CertificateResponse getCertificateById(Long certificateId) {
        log.info("🔍 내용증명서 조회 요청 - Certificate ID: {}", certificateId);
        CertificateEntity certificate = certificateRepository.findById(certificateId)
                .orElseThrow(() -> new RuntimeException("내용증명서를 찾을 수 없습니다: " + certificateId));
        return convertToResponse(certificate);
    }

    /**
     * READ - 사용자별 내용증명서 목록 조회
     */
    public List<CertificateResponse> getCertificatesByUserId(String userId) {
        log.info("👤 사용자별 내용증명서 조회 요청 - User ID: {}", userId);
        List<CertificateEntity> certificates = certificateRepository.findByUserId(userId);
        return certificates.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * DELETE - 내용증명서 삭제
     */
    @Transactional
    public boolean deleteCertificate(Long certificateId, String userId) {
        log.info("🗑️ 내용증명서 삭제 요청 - Certificate ID: {}, User ID: {}", certificateId, userId);
        CertificateEntity certificate = certificateRepository.findById(certificateId)
                .orElseThrow(() -> new RuntimeException("내용증명서를 찾을 수 없습니다: " + certificateId));

        if (!certificate.getUserId().equals(userId)) {
            throw new RuntimeException("해당 내용증명서를 삭제할 권한이 없습니다.");
        }
        certificateRepository.delete(certificate);
        log.info("✅ 내용증명서 삭제 완료 - Certificate ID: {}", certificateId);
        return true;
    }

    /**
     * UPDATE - 내용증명서 부분 수정
     */
    @Transactional
    public CertificateResponse updateCertificatePartial(Long certificateId, CertificateUpdateRequest request) {
        CertificateEntity certificate = certificateRepository.findById(certificateId)
                .orElseThrow(() -> new RuntimeException("내용증명서를 찾을 수 없습니다. ID: " + certificateId));

        if (request.getTitle() != null) certificate.setTitle(request.getTitle());
        if (request.getBody() != null) certificate.setBody(request.getBody());
        if (request.getStrategySummary() != null) certificate.setStrategySummary(request.getStrategySummary());
        if (request.getFollowupStrategy() != null) certificate.setFollowupStrategy(request.getFollowupStrategy());

        try {
            if (request.getReceiver() != null) {
                certificate.setReceiverJson(objectMapper.writeValueAsString(request.getReceiver()));
            }
            if (request.getSender() != null) {
                certificate.setSenderJson(objectMapper.writeValueAsString(request.getSender()));
            }
        } catch (JsonProcessingException e) {
            log.error("JSON 직렬화 실패", e);
            throw new RuntimeException("요청 데이터 처리 중 오류 발생");
        }

        CertificateEntity saved = certificateRepository.save(certificate);
        return convertToResponse(saved);
    }

    // Private helper methods
    private Map<String, Object> buildCertificateRequest(ContractData contractData, CertificateCreateRequest request) {
        Map<String, Object> mlRequest = new HashMap<>();
        mlRequest.put("user_query", request.getUserQuery());
        Map<String, Object> contractInfo = new HashMap<>();
        contractInfo.put("id", contractData.getId() != null ? contractData.getId().toString() : null);
        contractInfo.put("user_id", request.getUserId());
        contractInfo.put("contract_type", contractData.getContractType());
        if (contractData.getDates() != null) {
            Map<String, Object> dates = new HashMap<>();
            dates.put("contract_date", contractData.getDates().getContractDate());
            dates.put("start_date", contractData.getDates().getStartDate());
            dates.put("end_date", contractData.getDates().getEndDate());
            contractInfo.put("dates", dates);
        }
        if (contractData.getProperty() != null) {
            Map<String, Object> property = new HashMap<>();
            property.put("address", contractData.getProperty().getAddress());
            property.put("detail_address", contractData.getProperty().getDetailAddress());
            property.put("rent_section", contractData.getProperty().getRentSection());
            property.put("rent_area", contractData.getProperty().getRentArea());
            contractInfo.put("property", property);
        }
        if (contractData.getPayment() != null) {
            Map<String, Object> payment = new HashMap<>();
            payment.put("deposit", contractData.getPayment().getDeposit());
            payment.put("deposit_kr", contractData.getPayment().getDepositKr());
            payment.put("monthly_rent", contractData.getPayment().getMonthlyRent());
            payment.put("monthly_rent_date", contractData.getPayment().getMonthlyRentDate());
            contractInfo.put("payment", payment);
        }
        if (contractData.getLessor() != null) {
            Map<String, Object> lessor = new HashMap<>();
            lessor.put("name", contractData.getLessor().getName());
            lessor.put("address", contractData.getLessor().getAddress());
            lessor.put("phone_number", contractData.getLessor().getPhoneNumber());
            contractInfo.put("lessor", lessor);
        }
        if (contractData.getLessee() != null) {
            Map<String, Object> lessee = new HashMap<>();
            lessee.put("name", contractData.getLessee().getName());
            lessee.put("address", contractData.getLessee().getAddress());
            lessee.put("phone_number", contractData.getLessee().getPhoneNumber());
            contractInfo.put("lessee", lessee);
        }
        
        try {
            List<String> articles = contractData.getArticlesJson() != null ? 
                objectMapper.readValue(contractData.getArticlesJson(), new com.fasterxml.jackson.core.type.TypeReference<List<String>>() {}) : 
                new java.util.ArrayList<>();
            contractInfo.put("articles", articles);

            List<String> agreements = contractData.getAgreementsJson() != null ? 
                objectMapper.readValue(contractData.getAgreementsJson(), new com.fasterxml.jackson.core.type.TypeReference<List<String>>() {}) : 
                new java.util.ArrayList<>();
            contractInfo.put("agreements", agreements);
        } catch (JsonProcessingException e) {
            log.error("계약서의 articles/agreements JSON 파싱 실패", e);
            // 기본적으로 빈 리스트를 넣거나, 예외 처리를 할 수 있습니다.
            contractInfo.put("articles", new java.util.ArrayList<>());
            contractInfo.put("agreements", new java.util.ArrayList<>());
        }
        
        contractInfo.put("generated", contractData.getIsGenerated());
        contractInfo.put("file_url", contractData.getFileUrl());
        contractInfo.put("created_date", contractData.getCreatedDate() != null ? contractData.getCreatedDate().toString() : null);
        contractInfo.put("modified_date", contractData.getModifiedDate() != null ? contractData.getModifiedDate().toString() : null);
        mlRequest.put("contract_data", contractInfo);
        mlRequest.put("debug_mode", request.isDebugMode());
        log.info("📤 ML API 내용증명서 생성 요청 데이터 구성 완료 - Contract Type: {}, Debug Mode: {}",
                contractData.getContractType(), request.isDebugMode());
        return mlRequest;
    }

    private CertificateEntity saveCertificateResult(ContractData contractData, Map<String, Object> mlResponse, CertificateCreateRequest request) {
        CertificateEntity.CertificateEntityBuilder builder = CertificateEntity.builder()
                .contractId(contractData.getId().toString())
                .userId(request.getUserId())
                .userQuery(request.getUserQuery())
                .createdDate(LocalDateTime.now(ZoneOffset.UTC))
                .status("SUCCESS");

        try {
            // ML API 응답 데이터 매핑
            if (mlResponse.get("id") instanceof Number) {
                builder.mlCertificateId(((Number) mlResponse.get("id")).intValue());
            }
            builder.mlCreatedDate((String) mlResponse.get("created_date"));
            builder.title((String) mlResponse.get("title"));
            builder.body((String) mlResponse.get("body"));
            builder.strategySummary((String) mlResponse.get("strategy_summary"));
            builder.followupStrategy((String) mlResponse.get("followup_strategy"));

            // JSON 필드 변환 및 설정
            builder.receiverJson(toJson(mlResponse.get("receiver")));
            builder.senderJson(toJson(mlResponse.get("sender")));
            builder.legalBasisJson(toJson(mlResponse.get("legal_basis")));
            builder.caseBasisJson(toJson(mlResponse.get("case_basis")));
            builder.certificationMetadataJson(toJson(mlResponse.get("certification_metadata")));
            builder.rawMlResponseJson(toJson(mlResponse));

            CertificateEntity certificate = builder.build();
            return certificateRepository.save(certificate);

        } catch (Exception e) {
            log.error("❌ ML 내용증명서 응답 저장 실패", e);
            return certificateRepository.save(CertificateEntity.builder()
                    .contractId(contractData.getId().toString())
                    .userId(request.getUserId())
                    .userQuery(request.getUserQuery())
                    .createdDate(LocalDateTime.now(ZoneOffset.UTC))
                    .status("FAILED")
                    .errorMessage(e.getMessage())
                    .build());
        }
    }

    private CertificateResponse convertToResponse(CertificateEntity certificate) {
        return CertificateResponse.fromEntity(certificate);
    }

    private String toJson(Object obj) {
        if (obj == null) return null;
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.warn("JSON 직렬화 실패: {}", e.getMessage());
            return null;
        }
    }
} 