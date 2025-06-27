package com.superlawva.domain.ml.service;

import com.superlawva.domain.ml.client.MLApiClient;
import com.superlawva.domain.ml.dto.ContractCreateRequest;
import com.superlawva.domain.ml.dto.ContractResponse;
import com.superlawva.domain.ml.dto.ContractUpdateRequest;
import com.superlawva.domain.ocr3.entity.ContractData;
import com.superlawva.domain.ocr3.repository.ContractDataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContractService {
    private final ContractDataRepository contractDataRepository;
    private final MLApiClient mlApiClient;

    @Transactional
    public ContractResponse createContract(ContractCreateRequest request) {
        // 1. ML API 호출
        Map<String, Object> mlRequest = Map.of(
                "user_id", request.getUserId(),
                "user_query", request.getUserQuery()
        );
        Map<String, Object> mlResponse = mlApiClient.generateSpecialTerms(mlRequest);

        // 2. ContractData 생성 및 저장
        ContractData contract = new ContractData();
        contract.setUserId(request.getUserId());
        contract.setContractType("임대차");
        contract.setArticles(request.getArticles() != null ? request.getArticles() : Collections.emptyList());
        contract.setGenerated(true);
        // ML 결과 필드 저장
        ContractResponse mlDto = toContractResponseFromML(contract, mlResponse);
        contract.setRecommendedAgreements(mlDto.getRecommendedAgreements());
        contract.setLegalBasis(mlDto.getLegalBasis());
        contract.setCaseBasis(mlDto.getCaseBasis());
        contract.setAnalysisMetadata(mlDto.getAnalysisMetadata());
        contract.setCreatedDate(LocalDateTime.now(ZoneOffset.UTC));
        contract.setModifiedDate(LocalDateTime.now(ZoneOffset.UTC));
        ContractData saved = contractDataRepository.save(contract);

        // 3. ML 응답을 ContractResponse로 변환하여 반환
        return toContractResponseFromML(saved, mlResponse);
    }

    public ContractResponse getContractById(String id) {
        ContractData contract = contractDataRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("계약서를 찾을 수 없습니다. ID: " + id));
        // DB에 ML 원본 응답이 저장되어 있다면, 그걸 활용해도 됨
        return ContractResponse.fromEntity(contract);
    }

    public List<ContractResponse> getContractsByUserId(String userId) {
        return contractDataRepository.findByUserId(userId).stream()
                .map(ContractResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public ContractResponse updateContract(String id, ContractUpdateRequest request) {
        ContractData contract = contractDataRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("계약서를 찾을 수 없습니다. ID: " + id));

        // 선택적으로 필드를 업데이트
        if (request.getContractType() != null) {
            contract.setContractType(request.getContractType());
        }
        if (request.getArticles() != null) {
            contract.setArticles(request.getArticles());
        }
        if (request.getAgreements() != null) {
            contract.setAgreements(request.getAgreements());
        }

        // dates
        if (request.getDates() != null) {
            contract.setDates(request.getDates());
        }

        // property
        if (request.getProperty() != null) {
            contract.setProperty(request.getProperty());
        }

        // payment
        if (request.getPayment() != null) {
            contract.setPayment(request.getPayment());
        }

        // parties
        if (request.getLessor() != null) {
            contract.setLessor(request.getLessor());
        }
        if (request.getLessee() != null) {
            contract.setLessee(request.getLessee());
        }

        // brokers
        if (request.getBroker1() != null) {
            contract.setBroker1(request.getBroker1());
        }
        if (request.getBroker2() != null) {
            contract.setBroker2(request.getBroker2());
        }

        // recommended agreements
        if (request.getRecommendedAgreements() != null) {
            contract.setRecommendedAgreements(request.getRecommendedAgreements());
        }

        if (request.getLegalBasis() != null) {
            contract.setLegalBasis(request.getLegalBasis());
        }

        if (request.getCaseBasis() != null) {
            contract.setCaseBasis(request.getCaseBasis());
        }

        contract.setModifiedDate(LocalDateTime.now(ZoneOffset.UTC));
        ContractData saved = contractDataRepository.save(contract);
        return ContractResponse.fromEntity(saved);
    }

    @Transactional
    public void deleteContract(String id) {
        if (!contractDataRepository.existsById(id)) {
            throw new RuntimeException("삭제할 계약서를 찾을 수 없습니다. ID: " + id);
        }
        contractDataRepository.deleteById(id);
    }

    // ML API 응답을 ContractResponse로 변환
    private ContractResponse toContractResponseFromML(ContractData contract, Map<String, Object> mlResponse) {
        ContractResponse dto = new ContractResponse();
        dto.setId(contract.get_id());
        dto.setUserId(contract.getUserId());
        dto.setContractType(contract.getContractType());
        dto.setArticles(contract.getArticles());
        dto.setCreatedDate(contract.getCreatedDate());
        dto.setModifiedDate(contract.getModifiedDate());

        Object dataObj = mlResponse.get("data");
        if (dataObj instanceof Map) {
            Map<String, Object> data = (Map<String, Object>) dataObj;
            // recommended_agreements
            Object agreementsObj = data.get("recommended_agreements");
            if (agreementsObj instanceof List) {
                List<Map<String, Object>> agreementsList = (List<Map<String, Object>>) agreementsObj;
                List<ContractResponse.RecommendedAgreementDto> agreements = agreementsList.stream().map(a -> {
                    ContractResponse.RecommendedAgreementDto dtoA = new ContractResponse.RecommendedAgreementDto();
                    dtoA.setReason((String) a.get("reason"));
                    dtoA.setSuggestedRevision((String) a.get("suggested_revision"));
                    dtoA.setNegotiationPoints((String) a.get("negotiation_points"));
                    return dtoA;
                }).collect(Collectors.toList());
                dto.setRecommendedAgreements(agreements);
            }
            // legal_basis
            Object legalObj = data.get("legal_basis");
            if (legalObj instanceof List) {
                List<Map<String, Object>> legalList = (List<Map<String, Object>>) legalObj;
                List<ContractResponse.LegalBasisDto> legalDtos = legalList.stream().map(l -> {
                    ContractResponse.LegalBasisDto dtoL = new ContractResponse.LegalBasisDto();
                    Object lawId = l.get("law_id");
                    dtoL.setLawId(lawId instanceof Number ? ((Number) lawId).longValue() : null);
                    dtoL.setLaw((String) l.get("law"));
                    dtoL.setExplanation((String) l.get("explanation"));
                    dtoL.setContent((String) l.get("content"));
                    return dtoL;
                }).collect(Collectors.toList());
                dto.setLegalBasis(legalDtos);
            }
            // case_basis
            Object caseObj = data.get("case_basis");
            if (caseObj instanceof List) {
                List<Map<String, Object>> caseList = (List<Map<String, Object>>) caseObj;
                List<ContractResponse.CaseBasisDto> caseDtos = caseList.stream().map(c -> {
                    ContractResponse.CaseBasisDto dtoC = new ContractResponse.CaseBasisDto();
                    Object caseId = c.get("case_id");
                    dtoC.setCaseId(caseId instanceof Number ? ((Number) caseId).longValue() : null);
                    dtoC.setCaseName((String) c.get("case"));
                    dtoC.setExplanation((String) c.get("explanation"));
                    dtoC.setLink((String) c.get("link"));
                    return dtoC;
                }).collect(Collectors.toList());
                dto.setCaseBasis(caseDtos);
            }
            // analysis_metadata
            Object metaObj = data.get("analysis_metadata");
            if (metaObj instanceof Map) {
                Map<String, Object> meta = (Map<String, Object>) metaObj;
                ContractResponse.AnalysisMetadataDto metaDto = new ContractResponse.AnalysisMetadataDto();
                metaDto.setModel((String) meta.get("model"));
                metaDto.setVersion((String) meta.get("version"));
                Object genTime = meta.get("generation_time");
                metaDto.setGenerationTime(genTime instanceof Number ? ((Number) genTime).doubleValue() : null);
                dto.setAnalysisMetadata(metaDto);
            }
        }
        return dto;
    }
} 