package com.superlawva.domain.search.controller;


import com.superlawva.domain.search.dto.SearchRequestDTO;
import com.superlawva.domain.search.dto.SearchResponseDTO;
import com.superlawva.domain.search.service.SearchService;
import com.superlawva.domain.user.entity.User;
import com.superlawva.global.exception.BaseException;
import com.superlawva.global.response.ApiResponse;
import com.superlawva.global.response.status.ErrorStatus;
import com.superlawva.global.security.annotation.LoginUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/search")
@RequiredArgsConstructor
@Tag(name = "🔍 Search API", description = "통합 검색 API")
public class SearchController {
    
    private final SearchService searchService;
    
    @Operation(
        summary = "🔍 법령/판례 검색", 
        description = """
        ## 📖 API 설명
        ML 팀의 벡터 검색 엔진을 사용하여 법령과 판례를 검색합니다.
        검색 결과는 **유사도(similarity) 기준 내림차순**으로 정렬되어 반환됩니다.
    
        ### 2. 검색 유형 설명
        - **`law`**: 법령만 검색 (법률, 시행령, 시행규칙 등)
        - **`case`**: 판례만 검색 (대법원, 고등법원 판결문)
        - **`both`**: 법령 + 판례 모두 검색 (추천)
        
        ### 3. 요청 방법
        - HTTP 메서드: POST
        - Content-Type: application/json
        
        ### 4. 에러 처리
        - **400**: 검색어가 비어있거나 잘못된 파라미터
        - **401**: JWT 토큰이 없거나 만료됨 → 로그인 페이지로 이동
        - **500**: 서버 오류 → "잠시 후 다시 시도해주세요" 안내
        """
    )
    @PostMapping("/search")
    public ResponseEntity<ApiResponse<SearchResponseDTO>> searchDocuments(
            @Valid @RequestBody SearchRequestDTO request,
            @Parameter(hidden = true) @LoginUser User user
    ) {
        log.info("검색 요청 - Query: '{}', Type: '{}', k: {}, User: {}", 
                 request.query(), request.search_type(), request.k(), (user != null ? user.getId() : "Anonymous"));
        
        SearchResponseDTO response = searchService.search(request, user);
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }
} 