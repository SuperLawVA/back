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

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Validated
@Slf4j
@CrossOrigin(origins = {"http://localhost:3000", "http://127.0.0.1:3000"}) // 프론트엔드 CORS 설정
public class WordsController {
    
    private final WordsService wordsService;
    
    /**
     * 용어 검색 API
     * GET /api/terms/search?keyword=검색어&page=1&pageSize=10
     */
    @GetMapping("/terms/search")
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
     * 인기 검색어 API
     * GET /api/search-keywords/popular
     */
    @GetMapping("/search-keywords/popular")
    public ResponseEntity<PopularKeywordsResponseDto> getPopularKeywords() {
        PopularKeywordsResponseDto responseDto = wordsService.getPopularKeywords();
        return ResponseEntity.ok(responseDto);
    }
    
    /**
     * 용어 등록 API
     * POST /api/upload/words
     */
    @PostMapping("/upload/words")
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
     * 용어 상세 조회 API
     * GET /api/terms/{word}
     */
    @GetMapping("/terms/{word}")
    public ResponseEntity<WordsDto> getTermDetail(@PathVariable String word) {
        try {
            WordsDto responseDto = wordsService.getWordDetail(word);
            return ResponseEntity.ok(responseDto);
        } catch (IllegalArgumentException e) {
            log.warn("용어 조회 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }
    
    /**
     * 헬스 체크 API
     * GET /api/health
     */
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Words API is running!");
    }
}