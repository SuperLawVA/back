package com.superlawva.domain.ml.service;

import com.superlawva.domain.ml.client.MLApiClient;
import com.superlawva.domain.ml.dto.CertificateCreateRequest;
import com.superlawva.domain.ml.dto.CertificateResponse;
import com.superlawva.domain.ml.dto.CertificateUpdateRequest;
import com.superlawva.domain.ml.entity.Certificate;
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
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CertificateService {
    
    private final CertificateRepository certificateRepository;
    private final ContractDataRepository contractDataRepository;
    private final MLApiClient mlApiClient;
    
    /**
     * CREATE - 내용증명서 생성
     */
    @Transactional
    public CertificateResponse createCertificate(CertificateCreateRequest request) {
        log.info("📝 내용증명서 생성 요청 - Contract ID: {}", request.getContractId());
        
        try {
            // 1. Contract 조회
            ContractData contract = contractDataRepository.findById(request.getContractId())
                    .orElseThrow(() -> new RuntimeException("계약서를 찾을 수 없습니다: " + request.getContractId()));
            
            // Contract에서 userId 가져와서 request에 설정
            request.setUserId(contract.getUserId());
            
            log.info("📋 계약서 조회 완료 - User ID: {}, Contract Type: {}", 
                    contract.getUserId(), contract.getContractType());
            
            // 2. ML API용 데이터 준비
            Map<String, Object> mlRequestData = buildCertificateRequest(contract, request);
            
            // 3. ML API 호출 (내용증명서 생성)
            Map<String, Object> mlResponse = mlApiClient.generateProofDocument(mlRequestData);
            
            // 4. Certificate 엔티티 생성 및 저장
            Certificate savedCertificate = saveCertificateResult(contract, mlResponse, request);
            
            log.info("✅ 내용증명서 생성 완료 - Certificate ID: {}", savedCertificate.getId());
            
            // 5. 응답 DTO로 변환
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
    public CertificateResponse getCertificateById(String certificateId) {
        log.info("🔍 내용증명서 조회 요청 - Certificate ID: {}", certificateId);
        
        Certificate certificate = certificateRepository.findById(certificateId)
                .orElseThrow(() -> new RuntimeException("내용증명서를 찾을 수 없습니다: " + certificateId));
        
        return convertToResponse(certificate);
    }
    
    /**
     * READ - 사용자별 내용증명서 목록 조회
     */
    public List<CertificateResponse> getCertificatesByUserId(String userId) {
        log.info("👤 사용자별 내용증명서 조회 요청 - User ID: {}", userId);
        
        List<Certificate> certificates = certificateRepository.findByUserId(userId);
        
        return certificates.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * DELETE - 내용증명서 삭제
     */
    @Transactional
    public boolean deleteCertificate(String certificateId, String userId) {
        log.info("🗑️ 내용증명서 삭제 요청 - Certificate ID: {}, User ID: {}", certificateId, userId);
        
        Certificate certificate = certificateRepository.findById(certificateId)
                .orElseThrow(() -> new RuntimeException("내용증명서를 찾을 수 없습니다: " + certificateId));
        
        // 사용자 권한 확인
        if (!certificate.getUserId().equals(userId)) {
            throw new RuntimeException("해당 내용증명서를 삭제할 권한이 없습니다.");
        }
        
        certificateRepository.delete(certificate);
        
        log.info("✅ 내용증명서 삭제 완료 - Certificate ID: {}", certificateId);
        
        return true;
    }
    
    // 부분 수정 메서드
    @Transactional
    public CertificateResponse updateCertificatePartial(String certificateId, CertificateUpdateRequest request) {
        Certificate certificate = certificateRepository.findById(certificateId)
                .orElseThrow(() -> new RuntimeException("내용증명서를 찾을 수 없습니다. ID: " + certificateId));

        if (request.getTitle() != null) certificate.setTitle(request.getTitle());
        if (request.getUserQuery() != null) certificate.setUserQuery(request.getUserQuery());
        if (request.getBody() != null) certificate.setBody(request.getBody());
        if (request.getStrategySummary() != null) certificate.setStrategySummary(request.getStrategySummary());
        if (request.getFollowupStrategy() != null) certificate.setFollowupStrategy(request.getFollowupStrategy());
        if (request.getReceiver() != null) {
            Certificate.Receiver r = new Certificate.Receiver();
            r.setName(request.getReceiver().getName());
            r.setAddress(request.getReceiver().getAddress());
            r.setDetailAddress(request.getReceiver().getDetailAddress());
            certificate.setReceiver(r);
        }
        if (request.getSender() != null) {
            Certificate.Sender s = new Certificate.Sender();
            s.setName(request.getSender().getName());
            s.setAddress(request.getSender().getAddress());
            s.setDetailAddress(request.getSender().getDetailAddress());
            certificate.setSender(s);
        }
        if (request.getLegalBasis() != null) {
            List<Certificate.LegalBasis> legalList = new java.util.ArrayList<>();
            for (var dto : request.getLegalBasis()) {
                Certificate.LegalBasis l = new Certificate.LegalBasis();
                l.setLawId(dto.getLawId());
                l.setLaw(dto.getLaw());
                l.setExplanation(dto.getExplanation());
                l.setContent(dto.getContent());
                legalList.add(l);
            }
            certificate.setLegalBasis(legalList);
        }
        if (request.getCaseBasis() != null) {
            List<Certificate.CaseBasis> caseList = new java.util.ArrayList<>();
            for (var dto : request.getCaseBasis()) {
                Certificate.CaseBasis c = new Certificate.CaseBasis();
                c.setCaseId(dto.getCaseId());
                c.setCaseName(dto.getCaseName());
                c.setExplanation(dto.getExplanation());
                c.setLink(dto.getLink());
                caseList.add(c);
            }
            certificate.setCaseBasis(caseList);
        }
        if (request.getCertificationMetadata() != null) {
            var metaDto = request.getCertificationMetadata();
            Certificate.CertificationMetadata meta = new Certificate.CertificationMetadata();
            meta.setModel(metaDto.getModel());
            meta.setGenerationTime(metaDto.getGenerationTime());
            meta.setUserAgent(metaDto.getUserAgent());
            meta.setVersion(metaDto.getVersion());
            certificate.setCertificationMetadata(meta);
        }
        Certificate saved = certificateRepository.save(certificate);
        return convertToResponse(saved);
    }
    
    // Private helper methods
    private Map<String, Object> buildCertificateRequest(ContractData contractData, CertificateCreateRequest request) {
        Map<String, Object> mlRequest = new HashMap<>();
        
        // 사용자 요청사항
        mlRequest.put("user_query", request.getUserQuery());
        
        // contract_data 객체 생성 (ML API가 기대하는 구조)
        Map<String, Object> contractInfo = new HashMap<>();
        contractInfo.put("id", contractData.get_id());
        contractInfo.put("user_id", request.getUserId());
        contractInfo.put("contract_type", contractData.getContractType());
        
        // Dates 변환
        if (contractData.getDates() != null) {
            Map<String, Object> dates = new HashMap<>();
            dates.put("contract_date", contractData.getDates().getContractDate());
            dates.put("start_date", contractData.getDates().getStartDate());
            dates.put("end_date", contractData.getDates().getEndDate());
            contractInfo.put("dates", dates);
        }
        
        // Property 변환
        if (contractData.getProperty() != null) {
            Map<String, Object> property = new HashMap<>();
            property.put("address", contractData.getProperty().getAddress());
            property.put("detail_address", contractData.getProperty().getDetailAddress());
            property.put("rent_section", contractData.getProperty().getRentSection());
            property.put("rent_area", contractData.getProperty().getRentArea());
            contractInfo.put("property", property);
        }
        
        // Payment 변환
        if (contractData.getPayment() != null) {
            Map<String, Object> payment = new HashMap<>();
            payment.put("deposit", contractData.getPayment().getDeposit());
            payment.put("deposit_kr", contractData.getPayment().getDepositKr());
            payment.put("monthly_rent", contractData.getPayment().getMonthlyRent());
            payment.put("monthly_rent_date", contractData.getPayment().getMonthlyRentDate());
            contractInfo.put("payment", payment);
        }
        
        // Lessor/Lessee 변환
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
        
        // Articles/Agreements
        contractInfo.put("articles", contractData.getArticles() != null ? contractData.getArticles() : new java.util.ArrayList<>());
        contractInfo.put("agreements", contractData.getAgreements() != null ? contractData.getAgreements() : new java.util.ArrayList<>());
        
        // 추가 필드들
        contractInfo.put("generated", contractData.getGenerated());
        contractInfo.put("file_url", contractData.getFileUrl());
        contractInfo.put("created_date", contractData.getCreatedDate() != null ? contractData.getCreatedDate().toString() : null);
        contractInfo.put("modified_date", contractData.getModifiedDate() != null ? contractData.getModifiedDate().toString() : null);
        
        // contract_data를 최종 요청에 추가
        mlRequest.put("contract_data", contractInfo);
        
        // debug_mode 추가 (analysis와 동일한 구조)
        mlRequest.put("debug_mode", request.isDebugMode());
        
        log.info("📤 ML API 내용증명서 생성 요청 데이터 구성 완료 - Contract Type: {}, Debug Mode: {}", 
                contractData.getContractType(), request.isDebugMode());
        
        return mlRequest;
    }
    
    private Certificate saveCertificateResult(ContractData contractData, Map<String, Object> mlResponse, CertificateCreateRequest request) {
        try {
            log.info("💾 ML 내용증명서 응답을 Certificate 엔티티로 저장 시작");
            
            Certificate certificate = new Certificate();
            certificate.setContractId(contractData.get_id());
            certificate.setUserId(request.getUserId());
            certificate.setUserQuery(request.getUserQuery());
            certificate.setCreatedDate(LocalDateTime.now(ZoneOffset.UTC));
            certificate.setStatus("SUCCESS");
            
            // ML API 응답 데이터 매핑
            if (mlResponse.get("id") instanceof Number) {
                certificate.setMlCertificateId(((Number) mlResponse.get("id")).intValue());
            }
            
            // ML API에서 받은 created_date 설정
            certificate.setMlCreatedDate((String) mlResponse.get("created_date"));
            
            // 기본 내용증명서 정보
            certificate.setTitle((String) mlResponse.get("title"));
            certificate.setBody((String) mlResponse.get("body"));
            certificate.setStrategySummary((String) mlResponse.get("strategy_summary"));
            certificate.setFollowupStrategy((String) mlResponse.get("followup_strategy"));
            
            // 수신인 정보 매핑
            if (mlResponse.containsKey("receiver") && mlResponse.get("receiver") instanceof Map) {
                Map<String, Object> receiverMap = (Map<String, Object>) mlResponse.get("receiver");
                Certificate.Receiver receiver = new Certificate.Receiver();
                receiver.setName((String) receiverMap.get("name"));
                receiver.setAddress((String) receiverMap.get("address"));
                receiver.setDetailAddress((String) receiverMap.get("detail_address"));
                certificate.setReceiver(receiver);
            }
            
            // 발신인 정보 매핑
            if (mlResponse.containsKey("sender") && mlResponse.get("sender") instanceof Map) {
                Map<String, Object> senderMap = (Map<String, Object>) mlResponse.get("sender");
                Certificate.Sender sender = new Certificate.Sender();
                sender.setName((String) senderMap.get("name"));
                sender.setAddress((String) senderMap.get("address"));
                sender.setDetailAddress((String) senderMap.get("detail_address"));
                certificate.setSender(sender);
            }
            
            // 법적 근거 매핑
            if (mlResponse.containsKey("legal_basis") && mlResponse.get("legal_basis") instanceof List) {
                List<Map<String, Object>> legalBasisList = (List<Map<String, Object>>) mlResponse.get("legal_basis");
                List<Certificate.LegalBasis> legalBasis = legalBasisList.stream()
                        .map(legalMap -> {
                            Certificate.LegalBasis legal = new Certificate.LegalBasis();
                            if (legalMap.get("law_id") instanceof Number) {
                                legal.setLawId(((Number) legalMap.get("law_id")).intValue());
                            }
                            legal.setLaw((String) legalMap.get("law"));
                            legal.setExplanation((String) legalMap.get("explanation"));
                            legal.setContent((String) legalMap.get("content"));
                            return legal;
                        })
                        .collect(Collectors.toList());
                certificate.setLegalBasis(legalBasis);
            }
            
            // 판례 근거 매핑
            if (mlResponse.containsKey("case_basis") && mlResponse.get("case_basis") instanceof List) {
                List<Map<String, Object>> caseBasisList = (List<Map<String, Object>>) mlResponse.get("case_basis");
                List<Certificate.CaseBasis> caseBasis = caseBasisList.stream()
                        .map(caseMap -> {
                            Certificate.CaseBasis caseItem = new Certificate.CaseBasis();
                            if (caseMap.get("case_id") instanceof Number) {
                                caseItem.setCaseId(((Number) caseMap.get("case_id")).intValue());
                            }
                            caseItem.setCaseName((String) caseMap.get("case"));
                            caseItem.setExplanation((String) caseMap.get("explanation"));
                            caseItem.setLink((String) caseMap.get("link"));
                            return caseItem;
                        })
                        .collect(Collectors.toList());
                certificate.setCaseBasis(caseBasis);
            }
            
            // 메타데이터 매핑
            if (mlResponse.containsKey("certification_metadata") && mlResponse.get("certification_metadata") instanceof Map) {
                Map<String, Object> metadataMap = (Map<String, Object>) mlResponse.get("certification_metadata");
                Certificate.CertificationMetadata metadata = new Certificate.CertificationMetadata();
                metadata.setModel((String) metadataMap.get("model"));
                if (metadataMap.get("generation_time") instanceof Number) {
                    metadata.setGenerationTime(((Number) metadataMap.get("generation_time")).doubleValue());
                }
                metadata.setUserAgent((String) metadataMap.get("user_agent"));
                metadata.setVersion((String) metadataMap.get("version"));
                certificate.setCertificationMetadata(metadata);
            }
            

            
            // MongoDB에 저장
            Certificate savedCertificate = certificateRepository.save(certificate);
            
            log.info("✅ ML 내용증명서 응답 저장 완료 - Certificate ID: {}", savedCertificate.getId());
            log.info("   📋 Title: {}", savedCertificate.getTitle());
            log.info("   👤 Receiver: {}", savedCertificate.getReceiver() != null ? savedCertificate.getReceiver().getName() : "null");
            log.info("   📝 Strategy Summary: {}", savedCertificate.getStrategySummary() != null ? "존재" : "null");
            log.info("   ⚖️ Legal Basis 개수: {}", savedCertificate.getLegalBasis() != null ? savedCertificate.getLegalBasis().size() : 0);
            log.info("   📚 Case Basis 개수: {}", savedCertificate.getCaseBasis() != null ? savedCertificate.getCaseBasis().size() : 0);
            
            return savedCertificate;
            
        } catch (Exception e) {
            log.error("❌ ML 내용증명서 응답 저장 실패", e);
            
            // 실패 기록 저장
            Certificate failedCertificate = new Certificate();
            failedCertificate.setContractId(contractData.get_id());
            failedCertificate.setUserId(request.getUserId());
            failedCertificate.setUserQuery(request.getUserQuery());
            failedCertificate.setCreatedDate(LocalDateTime.now(ZoneOffset.UTC));
            failedCertificate.setStatus("FAILED");
            failedCertificate.setErrorMessage(e.getMessage());

            return certificateRepository.save(failedCertificate);
        }
    }
    
    private CertificateResponse convertToResponse(Certificate certificate) {
        CertificateResponse response = new CertificateResponse();
        
        // 기본 정보
        response.setId(certificate.getId());
        response.setMlCertificateId(certificate.getMlCertificateId());
        response.setContractId(certificate.getContractId());
        response.setUserId(certificate.getUserId());
        response.setUserQuery(certificate.getUserQuery());
        response.setCreatedDate(certificate.getCreatedDate());
        response.setMlCreatedDate(certificate.getMlCreatedDate());
        response.setStatus(certificate.getStatus());
        response.setErrorMessage(certificate.getErrorMessage());
        
        // ML API 응답 데이터
        response.setTitle(certificate.getTitle());
        response.setBody(certificate.getBody());
        response.setStrategySummary(certificate.getStrategySummary());
        response.setFollowupStrategy(certificate.getFollowupStrategy());
        
        // 수신인 정보 변환
        if (certificate.getReceiver() != null) {
            CertificateResponse.ReceiverDto receiverDto = new CertificateResponse.ReceiverDto();
            receiverDto.setName(certificate.getReceiver().getName());
            receiverDto.setAddress(certificate.getReceiver().getAddress());
            receiverDto.setDetailAddress(certificate.getReceiver().getDetailAddress());
            response.setReceiver(receiverDto);
        }
        
        // 발신인 정보 변환
        if (certificate.getSender() != null) {
            CertificateResponse.SenderDto senderDto = new CertificateResponse.SenderDto();
            senderDto.setName(certificate.getSender().getName());
            senderDto.setAddress(certificate.getSender().getAddress());
            senderDto.setDetailAddress(certificate.getSender().getDetailAddress());
            response.setSender(senderDto);
        }
        
        // 법적 근거 변환
        if (certificate.getLegalBasis() != null) {
            List<CertificateResponse.LegalBasisDto> legalBasisDtos = certificate.getLegalBasis().stream()
                    .map(legal -> {
                        CertificateResponse.LegalBasisDto dto = new CertificateResponse.LegalBasisDto();
                        dto.setLawId(legal.getLawId());
                        dto.setLaw(legal.getLaw());
                        dto.setExplanation(legal.getExplanation());
                        dto.setContent(legal.getContent());
                        return dto;
                    })
                    .collect(Collectors.toList());
            response.setLegalBasis(legalBasisDtos);
        }
        
        // 판례 근거 변환
        if (certificate.getCaseBasis() != null) {
            List<CertificateResponse.CaseBasisDto> caseBasisDtos = certificate.getCaseBasis().stream()
                    .map(caseItem -> {
                        CertificateResponse.CaseBasisDto dto = new CertificateResponse.CaseBasisDto();
                        dto.setCaseId(caseItem.getCaseId());
                        dto.setCaseName(caseItem.getCaseName());
                        dto.setExplanation(caseItem.getExplanation());
                        dto.setLink(caseItem.getLink());
                        return dto;
                    })
                    .collect(Collectors.toList());
            response.setCaseBasis(caseBasisDtos);
        }
        
        // 메타데이터 변환
        if (certificate.getCertificationMetadata() != null) {
            CertificateResponse.CertificationMetadataDto metadataDto = new CertificateResponse.CertificationMetadataDto();
            metadataDto.setModel(certificate.getCertificationMetadata().getModel());
            metadataDto.setGenerationTime(certificate.getCertificationMetadata().getGenerationTime());
            metadataDto.setUserAgent(certificate.getCertificationMetadata().getUserAgent());
            metadataDto.setVersion(certificate.getCertificationMetadata().getVersion());
            response.setCertificationMetadata(metadataDto);
        }
        
        return response;
    }
} 