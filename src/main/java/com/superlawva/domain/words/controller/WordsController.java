// WordsController.java
package com.superlawva.domain.words.controller;

import com.superlawva.domain.words.dto.*;
import com.superlawva.domain.words.service.WordsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;

@RestController
@RequestMapping("/words")
@RequiredArgsConstructor
@Validated
@Slf4j
@CrossOrigin(origins = {"http://localhost:3000", "http://127.0.0.1:3000"}) // 프론트엔드 CORS 설정
@Tag(name = "📚 Legal Terms", description = "법률 용어 검색 API")
public class WordsController {
    
    private final WordsService wordsService;
    
    /**
     * 용어 검색 API
     * GET /api/terms/search?keyword=검색어&page=1&pageSize=10
     */
    @GetMapping("/search")
    public ResponseEntity<WordsSearchResponseDto> searchTerms(
            @RequestParam(value = "keyword", defaultValue = "") String keyword,
            @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
            @RequestParam(value = "pageSize", defaultValue = "50") @Min(1) int pageSize) {
        
        // DTO 생성
        WordsSearchRequestDto requestDto = WordsSearchRequestDto.builder()
                .keyword(keyword)
                .page(page)
                .pageSize(Math.min(pageSize, 1000)) // 최대 1000개로 제한
                .build();
        
        // 서비스 호출
        WordsSearchResponseDto responseDto = wordsService.searchWords(requestDto);
        
        return ResponseEntity.ok(responseDto);
    }
    
    /**
     * 인기 키워드 조회
     * GET /words/popular
     */
    @GetMapping("/popular")
    public ResponseEntity<PopularKeywordsResponseDto> getPopularKeywords() {
        log.info("인기 키워드 조회 요청");
        PopularKeywordsResponseDto response = wordsService.getPopularKeywords();
        return ResponseEntity.ok(response);
    }
    
    /**
     * 용어 업로드 (관리자용)
     * POST /words/upload
     */
    @PostMapping("/upload")
    public ResponseEntity<WordsDto> uploadWords(@Valid @RequestBody WordsUploadRequestDto requestDto) {
        try {
            WordsDto responseDto = wordsService.uploadWord(requestDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
        } catch (IllegalArgumentException e) {
            log.warn("용어 등록 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(null);
        }
    }
    
    /**
     * 특정 용어 상세 조회
     * GET /words/{word}
     */
    @GetMapping("/{word}")
    public ResponseEntity<WordsDto> getWordDetail(@PathVariable String word) {
        log.info("특정 용어 조회 요청 - 단어: {}", word);
        WordsDto wordDetail = wordsService.getWordDetail(word);
        return ResponseEntity.ok(wordDetail);
    }
    
    /**
     * 용어 서비스 헬스체크
     * GET /words/health
     */
    @GetMapping("/health")
    public ResponseEntity<String> wordsHealthCheck() {
        return ResponseEntity.ok("Words API is running!");
    }
}