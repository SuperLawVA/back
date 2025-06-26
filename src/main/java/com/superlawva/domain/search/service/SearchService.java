package com.superlawva.domain.search.service;

import com.superlawva.domain.search.dto.SearchRequestDTO;
import com.superlawva.domain.search.dto.SearchResponseDTO;
import com.superlawva.domain.search.repository.CasesRepository;
import com.superlawva.domain.user.entity.User;
import com.superlawva.global.exception.BaseException;
import com.superlawva.global.response.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SearchService {

    private final RestTemplate restTemplate;
    private final CasesRepository casesRepository;

    @Value("${ml.api.base-url}")
    private String searchApiBaseUrl;

    public SearchResponseDTO search(SearchRequestDTO request, @Nullable User user) {
        long startTime = System.currentTimeMillis();

        if (request.query() == null || request.query().isBlank()) {
            throw new BaseException(ErrorStatus._BAD_REQUEST);
        }

        try {
            String url = searchApiBaseUrl + "/api/v1/search";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<SearchRequestDTO> httpEntity = new HttpEntity<>(request, headers);
            
            ResponseEntity<SearchResponseDTO> response = restTemplate.postForEntity(url, httpEntity, SearchResponseDTO.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                SearchResponseDTO mlResult = response.getBody();
                
                log.info("ML 검색 성공 - 사용자: {}, 질의: '{}', 결과: {}개", (user != null ? user.getId() : "Anonymous"), request.query(), mlResult.totalResults());

                List<SearchResponseDTO.DocumentResult> allDocuments = mlResult.documents().stream()
                        .map(this::enrichDocument)
                        .sorted(Comparator.comparing(SearchResponseDTO.DocumentResult::similarity).reversed())
                        .collect(Collectors.toList());

                List<SearchResponseDTO.DocumentResult> laws = allDocuments.stream()
                        .filter(doc -> "law".equalsIgnoreCase(doc.metadata().type()))
                        .collect(Collectors.toList());

                List<SearchResponseDTO.DocumentResult> cases = allDocuments.stream()
                        .filter(doc -> "case".equalsIgnoreCase(doc.metadata().type()))
                        .collect(Collectors.toList());

                double searchTimeSeconds = (System.currentTimeMillis() - startTime) / 1000.0;
                return new SearchResponseDTO(laws, cases, allDocuments, searchTimeSeconds, allDocuments.size());
            } else {
                log.error("ML 검색 API 호출 실패 - 상태코드: {}", response.getStatusCode());
                throw new BaseException(ErrorStatus.ML_API_CONNECTION_FAILED);
            }

        } catch (Exception e) {
            log.error("ML 검색 API 호출 중 오류 발생", e);
            throw new BaseException(ErrorStatus.ML_API_CONNECTION_FAILED);
        }
    }

    private SearchResponseDTO.DocumentResult enrichDocument(SearchResponseDTO.DocumentResult doc) {
        if ("case".equalsIgnoreCase(doc.metadata().type())) {
            String caseId = doc.metadata().caseId();
            if (caseId != null && !caseId.isEmpty()) {
                return casesRepository.findByCaseId(caseId)
                        .map(dbCase -> {
                            String newTitle = String.format("%s %s", dbCase.getCaseNumber(), dbCase.getCaseTitle());
                            return new SearchResponseDTO.DocumentResult(
                                    newTitle,
                                    dbCase.getSummary(),
                                    doc.similarity(),
                                    doc.metadata()
                            );
                        })
                        .orElse(doc);
            }
        }
        return doc;
    }
} 