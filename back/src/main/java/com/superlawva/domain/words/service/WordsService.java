// WordsService.java
package com.superlawva.domain.words.service;

import com.superlawva.domain.words.dto.*;
import com.superlawva.domain.words.entity.Words;
import com.superlawva.domain.words.repository.WordsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class WordsService {
    
    private final WordsRepository wordsRepository;
    
    /**
     * 용어 검색 (페이지네이션 + 정확도 정렬)
     */
    public WordsSearchResponseDto searchWords(WordsSearchRequestDto requestDto) {
        // 요청 데이터 유효성 검사 및 조정
        requestDto.validateAndAdjust();
        
        // 페이지네이션 설정 (Spring Data JPA는 0부터 시작)
        Pageable pageable = PageRequest.of(requestDto.getPage() - 1, requestDto.getPageSize());
        
        // 검색 실행
        Page<Words> wordsPage = wordsRepository.findByWordContainingOrderByExactMatchFirst(
                requestDto.getKeyword(), 
                requestDto.getKeyword(), 
                pageable);
        
        // DTO 변환
        List<WordsDto> wordsDtoList = wordsPage.getContent().stream()
                .map(WordsDto::fromEntity)
                .collect(Collectors.toList());
        
        log.info("용어 검색 실행 - 키워드: {}, 페이지: {}, 결과 수: {}", 
                requestDto.getKeyword(), requestDto.getPage(), wordsPage.getTotalElements());
        
        return WordsSearchResponseDto.builder()
                .totalResults(wordsPage.getTotalElements())
                .page(requestDto.getPage())
                .pageSize(requestDto.getPageSize())
                .data(wordsDtoList)
                .build();
    }
    
    /**
     * 인기 검색어 조회
     */
    public PopularKeywordsResponseDto getPopularKeywords() {
        // 현재는 하드코딩, 추후 검색 로그 기반으로 동적 생성 가능
        List<String> popularKeywords = Arrays.asList(
                "전세 보증금 미반환",
                "임대차 계약",
                "내용 증명",
                "보증금",
                "전세"
        );
        
        return PopularKeywordsResponseDto.builder()
                .keywords(popularKeywords)
                .build();
    }
    
    /**
     * 용어 등록
     */
    @Transactional
    public WordsDto uploadWord(WordsUploadRequestDto requestDto) {
        // 중복 체크
        if (wordsRepository.existsByWord(requestDto.getWord())) {
            throw new IllegalArgumentException("이미 존재하는 용어입니다: " + requestDto.getWord());
        }
        
        // Entity 생성 및 저장
        Words words = Words.builder()
                .word(requestDto.getWord())
                .content(requestDto.getContent())
                .build();
        
        Words savedWords = wordsRepository.save(words);
        
        log.info("새 용어 등록 완료 - 용어명: {}", savedWords.getWord());
        
        return WordsDto.fromEntity(savedWords);
    }
    
    /**
     * 용어 상세 조회
     */
    public WordsDto getWordDetail(String word) {
        Words words = wordsRepository.findById(word)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 용어입니다. 용어명: " + word));
        
        return WordsDto.fromEntity(words);
    }
}