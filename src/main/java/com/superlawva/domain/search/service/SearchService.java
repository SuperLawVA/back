package com.superlawva.domain.search.service;

import com.superlawva.domain.search.dto.MLSearchResponse;
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
import org.springframework.cache.annotation.Cacheable;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SearchService {

    private final RestTemplate restTemplate;
    private final CasesRepository casesRepository;

    @Value("${api.servers.search.base-url:${ml.api.base-url}}")
    private String searchApiBaseUrl;

    @Cacheable(value = "search", key = "#request.query() + ':' + #request.search_type() + ':' + #request.k()")
    @Transactional(readOnly = true)
    public SearchResponseDTO search(SearchRequestDTO request, @Nullable User user) {
        long startTime = System.currentTimeMillis();

        if (request.query() == null || request.query().trim().isEmpty()) {
            throw new BaseException(ErrorStatus.BAD_REQUEST);
        }

        try {
            String url = searchApiBaseUrl + "/api/v1/search";
            log.info("Attempting to call ML search API at: {}", url);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(List.of(MediaType.APPLICATION_JSON));
            HttpEntity<SearchRequestDTO> httpEntity = new HttpEntity<>(request, headers);
            ResponseEntity<MLSearchResponse> response = restTemplate.postForEntity(url, httpEntity, MLSearchResponse.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                MLSearchResponse mlResult = response.getBody();
                log.info("ML 검색 성공 - 사용자: {}, 질의: '{}', 결과: {}개", (user != null ? user.getId() : "Anonymous"), request.query(), mlResult.totalResults());

                List<SearchResponseDTO.DocumentResult> allDocuments = mlResult.documents().stream()
                        .map(this::convertToDocumentResult)
                        .map(this::enrichDocument)
                        .sorted(Comparator.comparing(SearchResponseDTO.DocumentResult::boostedSimilarity).reversed())
                        .collect(Collectors.toList());

                // Manual Pagination
                int totalResults = allDocuments.size();
                int page = request.page();
                int pageSize = request.pageSize();
                int totalPages = (int) Math.ceil((double) totalResults / pageSize);
                int start = (page - 1) * pageSize;
                int end = Math.min(start + pageSize, totalResults);

                List<SearchResponseDTO.DocumentResult> paginatedDocuments = (start > totalResults)
                        ? Collections.emptyList()
                        : allDocuments.subList(start, end);


                List<SearchResponseDTO.DocumentResult> laws = paginatedDocuments.stream()
                        .filter(doc -> "law".equalsIgnoreCase(doc.source()))
                        .collect(Collectors.toList());

                List<SearchResponseDTO.DocumentResult> cases = paginatedDocuments.stream()
                        .filter(doc -> "case".equalsIgnoreCase(doc.source()))
                        .collect(Collectors.toList());

                double searchTimeSeconds = (System.currentTimeMillis() - startTime) / 1000.0;
                return new SearchResponseDTO(laws, cases, paginatedDocuments, searchTimeSeconds, totalResults, page, pageSize, totalPages);
            } else {
                log.error("ML 검색 API 호출 실패 - 상태코드: {}", response.getStatusCode());
                throw new BaseException(ErrorStatus.ML_API_CONNECTION_FAILED);
            }

        } catch (Exception e) {
            log.error("ML 검색 API 호출 중 오류 발생", e);
            throw new BaseException(ErrorStatus.ML_API_CONNECTION_FAILED, e.getMessage());
        }
    }

    private SearchResponseDTO.DocumentResult convertToDocumentResult(MLSearchResponse.Document mlDoc) {
        Double boostedSimilarity = mlDoc.boostedSimilarity() != null ? mlDoc.boostedSimilarity() : mlDoc.similarity();

        return new SearchResponseDTO.DocumentResult(
                parseTitle(mlDoc.document()),
                parseContent(mlDoc.document()),
                mlDoc.similarity(),
                boostedSimilarity,
                mlDoc.source(),
                mlDoc.metadata()
        );
    }
    
    private SearchResponseDTO.DocumentResult enrichDocument(SearchResponseDTO.DocumentResult doc) {
        if ("case".equalsIgnoreCase(doc.source())) {
            Object caseIdObj = doc.metadata().get("case_id"); // ML 팀에서 사용하는 키가 case_id라고 가정
            if (caseIdObj instanceof String caseId && !caseId.isEmpty()) {
                return casesRepository.findByCaseId(caseId)
                        .map(dbCase -> new SearchResponseDTO.DocumentResult(
                                String.format("%s %s", dbCase.getCaseNumber(), dbCase.getCaseTitle()),
                                dbCase.getSummary(),
                                doc.similarity(),
                                doc.boostedSimilarity(),
                                doc.source(),
                                doc.metadata()
                        ))
                        .orElse(doc);
            }
        }
        return doc;
    }

    private String parseTitle(String document) {
        if (document == null) return "";
        String[] parts = document.split("\\n", 2);
        return parts.length > 0 ? parts[0].replace("제목:", "").trim() : document;
    }

    private String parseContent(String document) {
        if (document == null) return "";
        String[] parts = document.split("\\n", 2);
        return parts.length > 1 ? parts[1].replace("내용:", "").trim() : "";
    }
} 