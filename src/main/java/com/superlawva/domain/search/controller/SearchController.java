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
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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
        
        ## 🎯 프론트엔드 구현 가이드
        
        ### 1. 요청 방법
        ```javascript
        const searchData = {
            query: "임대차 보증금 반환",    // 필수: 검색어
            search_type: "both",           // 선택: law/case/both (기본값: both)
            k: 10                          // 선택: 결과 개수 1-20 (기본값: 10)
        };
        
        fetch('/api/v1/search', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': 'Bearer ' + JWT토큰
            },
            body: JSON.stringify(searchData)
        })
        ```
        
        ### 2. 검색 유형 설명
        - **`law`**: 법령만 검색 (법률, 시행령, 시행규칙 등)
        - **`case`**: 판례만 검색 (대법원, 고등법원 판결문)
        - **`both`**: 법령 + 판례 모두 검색 (추천)
        
        ### 3. 응답 데이터 활용
        ```javascript
        response.documents.forEach(doc => {
            console.log('제목:', doc.title);
            console.log('내용:', doc.content);
            console.log('유사도:', doc.similarity);  // 0.0 ~ 1.0
            console.log('문서타입:', doc.metadata.type);
        });
        ```
        
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
                examples = @ExampleObject(
                    name = "성공 응답 예시",
                    value = """
                    {
                        "documents": [
                            {
                                "title": "주택임대차보호법 제3조 (보증금의 반환 등)",
                                "content": "임대차가 종료한 때에는 임대인은 임차인에게 보증금을 반환하여야 한다. 다만, 임차인이 차임 또는 그 밖의 임대차에 관한 채무를 지급하지 아니한 때에는 임대인은 보증금에서 이를 공제할 수 있다.",
                                "similarity": 0.892,
                                "metadata": {
                                    "type": "law",
                                    "source": "주택임대차보호법",
                                    "section": "제3조",
                                    "url": "https://law.go.kr/법령/주택임대차보호법"
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
                                    "url": "https://casenote.kr/case/2023다12345"
                                }
                            }
                        ],
                        "search_time_seconds": 0.245,
                        "total_results": 15
                    }
                    """
                )
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
                        "documents": [
                            {
                                "title": "오류 발생",
                                "content": "검색어는 필수입니다.",
                                "similarity": 0.0,
                                "metadata": {
                                    "type": "error",
                                    "source": "system",
                                    "section": "",
                                    "url": ""
                                }
                            }
                        ],
                        "search_time_seconds": 0.0,
                        "total_results": 0
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
                        "documents": [
                            {
                                "title": "오류 발생",
                                "content": "검색 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.",
                                "similarity": 0.0,
                                "metadata": {
                                    "type": "error",
                                    "source": "system",
                                    "section": "",
                                    "url": ""
                                }
                            }
                        ],
                        "search_time_seconds": 0.0,
                        "total_results": 0
                    }
                    """
                )
            )
        )
    })
    @PostMapping("/search")
    @SecurityRequirement(name = "JWT")
    public ResponseEntity<SearchResponseDTO> searchDocuments(
            @Valid @RequestBody SearchRequestDTO request,
            @Parameter(hidden = true) @LoginUser User user
    ) {
        if (user == null) {
            throw new BaseException(ErrorStatus._UNAUTHORIZED);
        }
        
        log.info("법령/판례 검색 요청 - 사용자: {}, 질의: '{}'", user.getId(), request.query());
        
        SearchResponseDTO response = searchService.searchDocuments(request, user);
        return ResponseEntity.ok(response);
    }
} 