package com.superlawva.domain.ml.service;

import com.superlawva.domain.ml.client.MLApiClient;
import com.superlawva.domain.ml.dto.ContractCreateRequest;
import com.superlawva.domain.ml.dto.ContractResponse;
import com.superlawva.domain.ml.dto.ContractUpdateRequest;
import com.superlawva.domain.ocr3.entity.ContractData;
import com.superlawva.domain.ocr3.repository.ContractDataRepository;
import com.superlawva.global.exception.BaseException;
import com.superlawva.global.response.status.ErrorStatus;
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
        log.info("📝 계약서 생성 요청 - User ID: {}", request.getUserId());
        
        try {
            // 1. ML API 호출
            Map<String, Object> mlRequest = Map.of(
                    "user_id", String.valueOf(request.getUserId()),
                    "user_query", request.getUserQuery()
            );
            Map<String, Object> mlResponse = mlApiClient.generateSpecialTerms(mlRequest);

            // 2. ContractData 생성 및 저장
            ContractData contract = new ContractData();
            contract.setUserId(request.getUserId());
            contract.setContractType("임대차");
            // articles를 JSON으로 저장
            if (request.getArticles() != null) {
                contract.setArticlesJson(convertListToJson(request.getArticles()));
            }
            contract.setIsGenerated(true);
            contract.setCreatedDate(LocalDateTime.now(ZoneOffset.UTC));
            contract.setModifiedDate(LocalDateTime.now(ZoneOffset.UTC));
            
            // ML 응답을 기반으로 엔티티 필드 설정
            setContractDataFromML(contract, mlResponse);
            
            ContractData saved = contractDataRepository.save(contract);
            log.info("✅ 계약서 생성 완료 - Contract ID: {}", saved.getId());
            
            // 3. 저장된 엔티티와 ML 응답을 ContractResponse로 변환하여 반환
            return toContractResponseFromML(saved, mlResponse);
            
        } catch (Exception e) {
            log.error("❌ 계약서 생성 실패 - User ID: {}, Error: {}", request.getUserId(), e.getMessage());
            throw new BaseException(ErrorStatus.CONTRACT_CREATION_FAILED, "계약서 생성 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    public ContractResponse getContractById(String id) {
        log.info("🔍 계약서 조회 요청 - Contract ID: {}", id);
        
        try {
            // String을 Long으로 변환
            Long contractId = Long.parseLong(id);
            ContractData contract = contractDataRepository.findById(contractId)
                    .orElseThrow(() -> new BaseException(ErrorStatus.CONTRACT_NOT_FOUND, "계약서를 찾을 수 없습니다. ID: " + id));
            
            log.info("✅ 계약서 조회 완료 - Contract ID: {}", id);
            return ContractResponse.fromEntity(contract);
            
        } catch (NumberFormatException e) {
            log.error("❌ 잘못된 계약서 ID 형식 - ID: {}", id);
            throw new BaseException(ErrorStatus.BAD_REQUEST, "잘못된 계약서 ID 형식입니다: " + id);
        } catch (BaseException e) {
            throw e;
        } catch (Exception e) {
            log.error("❌ 계약서 조회 실패 - Contract ID: {}, Error: {}", id, e.getMessage());
            throw new BaseException(ErrorStatus.INTERNAL_SERVER_ERROR, "계약서 조회 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    public List<ContractResponse> getContractsByUserId(String userId) {
        log.info("👤 사용자별 계약서 조회 요청 - User ID: {}", userId);
        
        try {
            List<ContractResponse> contracts = contractDataRepository.findByUserId(userId).stream()
                    .map(ContractResponse::fromEntity)
                    .collect(Collectors.toList());
            
            log.info("✅ 사용자별 계약서 조회 완료 - User ID: {}, Count: {}", userId, contracts.size());
            return contracts;
            
        } catch (Exception e) {
            log.error("❌ 사용자별 계약서 조회 실패 - User ID: {}, Error: {}", userId, e.getMessage());
            throw new BaseException(ErrorStatus.INTERNAL_SERVER_ERROR, "사용자별 계약서 조회 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    @Transactional
    public ContractResponse updateContract(String id, ContractUpdateRequest request) {
        log.info("📝 계약서 수정 요청 - Contract ID: {}", id);
        
        try {
            // String을 Long으로 변환
            Long contractId = Long.parseLong(id);
            ContractData contract = contractDataRepository.findById(contractId)
                    .orElseThrow(() -> new BaseException(ErrorStatus.CONTRACT_NOT_FOUND, "계약서를 찾을 수 없습니다. ID: " + id));
            
            // 부분 수정 로직
            if (request.getArticles() != null) {
                contract.setArticlesJson(convertListToJson(request.getArticles()));
                log.info("📋 계약 조항 업데이트 - Count: {}", request.getArticles().size());
            }
            
            if (request.getUserQuery() != null) {
                // userQuery를 별도 필드로 저장 (임시로 articles에 저장)
                contract.setArticlesJson(convertListToJson(request.getUserQuery()));
                log.info("💬 사용자 쿼리 업데이트 - Count: {}", request.getUserQuery().size());
            }
            
            if (request.getContractType() != null) {
                contract.setContractType(request.getContractType());
                log.info("📄 계약 유형 업데이트 - Type: {}", request.getContractType());
            }
            
            if (request.getAgreements() != null) {
                contract.setAgreementsJson(convertListToJson(request.getAgreements()));
                log.info("📜 특약사항 업데이트 - Count: {}", request.getAgreements().size());
            }
            
            contract.setModifiedDate(LocalDateTime.now(ZoneOffset.UTC));
            ContractData saved = contractDataRepository.save(contract);
            
            log.info("✅ 계약서 수정 완료 - Contract ID: {}", saved.getId());
            return ContractResponse.fromEntity(saved);
            
        } catch (NumberFormatException e) {
            log.error("❌ 잘못된 계약서 ID 형식 - ID: {}", id);
            throw new BaseException(ErrorStatus.BAD_REQUEST, "잘못된 계약서 ID 형식입니다: " + id);
        } catch (BaseException e) {
            throw e;
        } catch (Exception e) {
            log.error("❌ 계약서 수정 실패 - Contract ID: {}, Error: {}", id, e.getMessage());
            throw new BaseException(ErrorStatus.INTERNAL_SERVER_ERROR, "계약서 수정 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    @Transactional
    public void deleteContract(String id) {
        log.info("🗑️ 계약서 삭제 요청 - Contract ID: {}", id);
        
        try {
            // String을 Long으로 변환
            Long contractId = Long.parseLong(id);
            if (!contractDataRepository.existsById(contractId)) {
                throw new BaseException(ErrorStatus.CONTRACT_NOT_FOUND, "삭제할 계약서를 찾을 수 없습니다. ID: " + id);
            }
            
            contractDataRepository.deleteById(contractId);
            log.info("✅ 계약서 삭제 완료 - Contract ID: {}", id);
            
        } catch (NumberFormatException e) {
            log.error("❌ 잘못된 계약서 ID 형식 - ID: {}", id);
            throw new BaseException(ErrorStatus.BAD_REQUEST, "잘못된 계약서 ID 형식입니다: " + id);
        } catch (BaseException e) {
            throw e;
        } catch (Exception e) {
            log.error("❌ 계약서 삭제 실패 - Contract ID: {}, Error: {}", id, e.getMessage());
            throw new BaseException(ErrorStatus.INTERNAL_SERVER_ERROR, "계약서 삭제 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    // ML API 응답을 ContractResponse로 변환
    private ContractResponse toContractResponseFromML(ContractData contract, Map<String, Object> mlResponse) {
        // 먼저 기본 엔티티 정보로 DTO를 초기화합니다.
        // fromEntity 메서드에서 이미 id.toString()을 처리합니다.
        ContractResponse dto = ContractResponse.fromEntity(contract);

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
    
    // ML 응답을 ContractData 엔티티에 채우는 헬퍼 메서드
    private void setContractDataFromML(ContractData contract, Map<String, Object> mlResponse) {
        Object dataObj = mlResponse.get("data");
        if (dataObj instanceof Map) {
            Map<String, Object> data = (Map<String, Object>) dataObj;
            
            Object agreementsObj = data.get("recommended_agreements");
            if (agreementsObj instanceof List) {
                contract.setRecommendedAgreementsJson(convertListToJson((List<?>) agreementsObj));
            }

            Object legalObj = data.get("legal_basis");
            if (legalObj instanceof List) {
                contract.setLegalBasisJson(convertListToJson((List<?>) legalObj));
            }
            
            Object caseObj = data.get("case_basis");
            if (caseObj instanceof List) {
                contract.setCaseBasisJson(convertListToJson((List<?>) caseObj));
            }
            
            Object metaObj = data.get("analysis_metadata");
            if (metaObj instanceof Map) {
                contract.setAnalysisMetadataJson(convertObjectToJson(metaObj));
            }
        }
    }

    private String convertListToJson(List<?> list) {
        // Implementation of convertListToJson method
        return null; // Placeholder return, actual implementation needed
    }

    private String convertObjectToJson(Object object) {
        // Implementation of convertObjectToJson method
        return null; // Placeholder return, actual implementation needed
    }

    private List<String> parseJsonToStringList(String json) {
        // Implementation of parseJsonToStringList method
        return Collections.emptyList(); // Placeholder return, actual implementation needed
    }
}