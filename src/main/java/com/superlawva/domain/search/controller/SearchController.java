package com.superlawva.domain.search.controller;


import com.superlawva.domain.search.dto.SearchRequestDTO;
import com.superlawva.domain.search.dto.SearchResponseDTO;
import com.superlawva.domain.search.service.SearchService;
import com.superlawva.domain.user.entity.User;
import com.superlawva.global.exception.BaseException;
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
        

        ### 4. 에러 처리
        - **400**: 검색어가 비어있거나 잘못된 파라미터
        - **401**: JWT 토큰이 없거나 만료됨 → 로그인 페이지로 이동
        - **500**: 서버 오류 → "잠시 후 다시 시도해주세요" 안내
        """
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "✅ 검색 성공",
            content = @Content(
                examples = {
                    @ExampleObject(
                        name = "검색 결과 있음 (유사도 내림차순 정렬)",
                        value = """
                        {
                            "laws": [
                                {
                                    "title": "주택임대차보호법 제3조 (보증금의 반환 등)",
                                    "content": "임대차가 종료한 때에는 임대인은 임차인에게 보증금을 반환하여야 한다. 다만, 임차인이 차임 또는 그 밖의 임대차에 관한 채무를 지급하지 아니한 때에는 임대인은 보증금에서 이를 공제할 수 있다.",
                                    "similarity": 0.892,
                                    "metadata": {
                                        "type": "law",
                                        "source": "주택임대차보호법",
                                        "section": "제3조",
                                        "url": "https://law.go.kr/법령/주택임대차보호법",
                                        "caseId": null
                                    }
                                }
                            ],
                            "cases": [
                                {
                                    "title": "2023다12345 보증금반환 청구의 소",
                                    "content": "임대차 계약이 종료되었음에도 불구하고 임대인이 보증금을 반환하지 않는 경우 임차인은 민사소송을 통해 보증금 반환을 청구할 수 있다.",
                                    "similarity": 0.756,
                                    "metadata": {
                                        "type": "case",
                                        "source": "대법원",
                                        "section": "2023.12.15",
                                        "url": "https://casenote.kr/case/2023다12345",
                                        "caseId": "2023다12345"
                                    }
                                }
                            ],
                            "documents": [
                                {
                                    "title": "주택임대차보호법 제3조 (보증금의 반환 등)",
                                    "content": "임대차가 종료한 때에는 임대인은 임차인에게 보증금을 반환하여야 한다. 다만, 임차인이 차임 또는 그 밖의 임대차에 관한 채무를 지급하지 아니한 때에는 임대인은 보증금에서 이를 공제할 수 있다.",
                                    "similarity": 0.892,
                                    "metadata": {
                                        "type": "law",
                                        "source": "주택임대차보호법",
                                        "section": "제3조",
                                        "url": "https://law.go.kr/법령/주택임대차보호법",
                                        "caseId": null
                                    }
                                },
                                {
                                    "title": "2023다12345 보증금반환 청구의 소",
                                    "content": "임대차 계약이 종료되었음에도 불구하고 임대인이 보증금을 반환하지 않는 경우 임차인은 민사소송을 통해 보증금 반환을 청구할 수 있다.",
                                    "similarity": 0.756,
                                    "metadata": {
                                        "type": "case",
                                        "source": "대법원",
                                        "section": "2023.12.15",
                                        "url": "https://casenote.kr/case/2023다12345",
                                        "caseId": "2023다12345"
                                    }
                                }
                            ],
                            "search_time_seconds": 0.245,
                            "total_results": 2
                        }
                        """
                    ),
                    @ExampleObject(
                        name = "검색 결과 없음",
                        value = """
                        {
                            "laws": [],
                            "cases": [],
                            "documents": [],
                            "search_time_seconds": 0.123,
                            "total_results": 0
                        }
                        """
                    )
                }
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", 
            description = "❌ 잘못된 요청 데이터",
            content = @Content(
                examples = @ExampleObject(
                    name = "검색어 누락 오류",
                    value = """
                    {
                        "isSuccess": false,
                        "code": "SEARCH400",
                        "message": "검색어를 입력해주세요.",
                        "result": null
                    }
                    """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401", 
            description = "❌ 인증 실패 (JWT 토큰 문제)",
            content = @Content(
                examples = @ExampleObject(
                    name = "인증 오류",
                    value = """
                    {
                        "isSuccess": false,
                        "code": "COMMON401",
                        "message": "인증이 필요합니다.",
                        "result": null
                    }
                    """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "500", 
            description = "❌ 서버 오류 (ML API 연결 실패 등)",
            content = @Content(
                examples = @ExampleObject(
                    name = "서버 오류",
                    value = """
                    {
                        "isSuccess": false,
                        "code": "SEARCH500",
                        "message": "검색 처리에 실패했습니다.",
                        "result": null
                    }
                    """
                )
            )
        )
    })
    @PostMapping("/search")
    public ResponseEntity<SearchResponseDTO> searchDocuments(
            @Valid @RequestBody SearchRequestDTO request,
            @Parameter(hidden = true) @LoginUser User user
    ) {
        log.info("검색 요청 - Query: '{}', Type: '{}', k: {}, User: {}", 
                 request.query(), request.search_type(), request.k(), (user != null ? user.getId() : "Anonymous"));
        
        SearchResponseDTO response = searchService.search(request, user);
        
        return ResponseEntity.ok(response);
    }
} 