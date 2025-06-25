package com.superlawva.domain.search.service;


import com.superlawva.domain.search.dto.SearchRequestDTO;
import com.superlawva.domain.search.dto.SearchResponseDTO;
import com.superlawva.domain.search.entity.Cases;
import com.superlawva.domain.search.repository.CasesRepository;
import com.superlawva.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchService {
    
    private final RestTemplate restTemplate;
    private final CasesRepository casesRepository;
    
    @Value("${chatbot.api.base-url}")
    private String mlApiBaseUrl;
    
    /**
     * ML 팀 검색 API 호출
     */
    public SearchResponseDTO searchDocuments(SearchRequestDTO request, User user) {
        log.info("사용자 {}의 문서 검색 요청: {}", user.getId(), request.query());
        
        try {
            // ML 팀 API 호출
            String url = mlApiBaseUrl + "/api/v1/search";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Accept", "application/json;charset=UTF-8");
            
            // 요청 바디 구성
            Map<String, Object> requestBody = Map.of(
                    "query", request.query(),
                    "search_type", request.search_type(),
                    "k", request.k()
            );
            
            HttpEntity<Map<String, Object>> httpEntity = new HttpEntity<>(requestBody, headers);
            
            long startTime = System.currentTimeMillis();
            ResponseEntity<SearchResponseDTO> response = restTemplate.postForEntity(
                    url, 
                    httpEntity, 
                    SearchResponseDTO.class
            );
            long endTime = System.currentTimeMillis();
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                SearchResponseDTO mlResult = response.getBody();
                double searchTimeSeconds = (endTime - startTime) / 1000.0;
                
                log.info("검색 성공 - 질의: '{}', 결과: {}개, 처리시간: {}ms", 
                         request.query(), mlResult.totalResults(), (endTime - startTime));
                
                // law와 case로 분류하고 유사도 기준 내림차순 정렬
                List<SearchResponseDTO.DocumentResult> laws = mlResult.documents().stream()
                        .filter(doc -> "law".equals(doc.metadata().type()))
                        .sorted(Comparator.comparing(SearchResponseDTO.DocumentResult::similarity).reversed())
                        .collect(Collectors.toList());
                
                List<SearchResponseDTO.DocumentResult> cases = mlResult.documents().stream()
                        .filter(doc -> "case".equals(doc.metadata().type()))
                        .map(this::enrichCaseWithDbInfo)  // Cases 테이블 연동 활성화
                        .sorted(Comparator.comparing(SearchResponseDTO.DocumentResult::similarity).reversed())
                        .collect(Collectors.toList());
                
                // 전체 documents도 유사도 기준 내림차순 정렬 (하위 호환성 유지)
                List<SearchResponseDTO.DocumentResult> sortedAllDocuments = mlResult.documents().stream()
                        .sorted(Comparator.comparing(SearchResponseDTO.DocumentResult::similarity).reversed())
                        .collect(Collectors.toList());
                
                // 새로운 응답 객체 생성 (law, case 구분 + 기존 documents 유지)
                SearchResponseDTO result = new SearchResponseDTO(
                        laws,
                        cases,
                        sortedAllDocuments, // 유사도 정렬된 전체 문서 목록
                        mlResult.searchTimeSeconds(),
                        mlResult.totalResults()
                );
                
                log.info("검색 결과 정렬 완료 - 법령: {}개, 판례: {}개 (유사도 내림차순)", 
                         laws.size(), cases.size());
                
                return result;
            } else {
                log.error("ML 팀 검색 API 호출 실패 - 상태코드: {}, 응답: {}", 
                         response.getStatusCode(), response.getBody());
                
                return createErrorResponse("검색 서비스에 일시적인 문제가 발생했습니다. (상태코드: " + response.getStatusCode() + ")");
            }
            
        } catch (Exception e) {
            log.error("ML 팀 검색 API 호출 중 오류 발생 - 에러: {}", e.getMessage(), e);
            
            // 네트워크 연결 에러인지 확인
            String errorMessage = e.getMessage().contains("Connection") ? 
                "ML 서버에 연결할 수 없습니다. 관리자에게 문의하세요." : 
                "검색 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.";
            
            return createErrorResponse(errorMessage);
        }
    }
    
    /**
     * 판례 정보를 RDS cases 테이블 정보로 보강
     */
    private SearchResponseDTO.DocumentResult enrichCaseWithDbInfo(SearchResponseDTO.DocumentResult caseDoc) {
        try {
            String caseId = caseDoc.metadata().caseId();
            if (caseId != null && !caseId.isEmpty()) {
                // RDS에서 해당 caseNumber로 추가 정보 조회 (case_number 컬럼과 매칭)
                Optional<Cases> caseEntity = casesRepository.findByCaseNumber(caseId);
                
                if (caseEntity.isPresent()) {
                    Cases cases = caseEntity.get();
                    
                    // 더 상세한 제목 생성 (사건번호 + 사건명)
                    String enhancedTitle = String.format("%s - %s", 
                        cases.getCaseNumber() != null ? cases.getCaseNumber() : "사건번호 미상",
                        cases.getCaseTitle() != null ? cases.getCaseTitle() : caseDoc.title()
                    );
                    
                    // 더 풍부한 콘텐츠 생성 (기존 + DB 정보)
                    StringBuilder enhancedContent = new StringBuilder();
                    enhancedContent.append("**사건정보**\n");
                    enhancedContent.append("- 사건번호: ").append(cases.getCaseNumber()).append("\n");
                    enhancedContent.append("- 사건유형: ").append(cases.getCaseType()).append("\n");
                    enhancedContent.append("- 판결일: ").append(cases.getDecisionDate()).append("\n\n");
                    enhancedContent.append("**판결내용**\n");
                    enhancedContent.append(caseDoc.content());
                    
                    // RDS 정보로 메타데이터 보강
                    SearchResponseDTO.DocumentMetadata enrichedMetadata = new SearchResponseDTO.DocumentMetadata(
                            caseDoc.metadata().type(),
                            cases.getCaseNumber(), // 사건번호로 업데이트
                            cases.getDecisionDate() != null ? cases.getDecisionDate().toString() : caseDoc.metadata().section(),
                            caseDoc.metadata().url(),
                            cases.getCaseId()
                    );
                    
                    return new SearchResponseDTO.DocumentResult(
                            enhancedTitle,
                            enhancedContent.toString(),
                            caseDoc.similarity(),
                            enrichedMetadata
                    );
                }
            }
            
            // RDS에서 찾지 못한 경우 원본 그대로 반환
            return caseDoc;
            
        } catch (Exception e) {
            log.warn("판례 정보 보강 중 오류 발생 - caseId: {}, 오류: {}", 
                     caseDoc.metadata().caseId(), e.getMessage());
            // 오류 시 원본 그대로 반환
            return caseDoc;
        }
    }
    
    /**
     * 오류 응답 생성
     */
    private SearchResponseDTO createErrorResponse(String errorMessage) {
        List<SearchResponseDTO.DocumentResult> errorDocuments = java.util.List.of(
                new SearchResponseDTO.DocumentResult(
                        "오류 발생",
                        errorMessage,
                        0.0,
                        new SearchResponseDTO.DocumentMetadata("error", "system", "", "", null)
                )
        );
        
        return new SearchResponseDTO(
                java.util.List.of(), // laws (빈 리스트)
                java.util.List.of(), // cases (빈 리스트)  
                errorDocuments,      // documents (에러 메시지)
                0.0,
                0
        );
    }
} 