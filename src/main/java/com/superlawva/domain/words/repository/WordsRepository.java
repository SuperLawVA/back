// ========================== Repository ==========================
// WordsRepository.java
package com.superlawva.domain.words.repository;

import com.superlawva.domain.words.entity.Words;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface WordsRepository extends JpaRepository<Words, String> {  // Primary Key가 String 타입
    
    // 키워드로 검색하되 정확일치를 우선으로 정렬
    @Query("SELECT w FROM Words w WHERE w.word LIKE %:keyword% " +
           "ORDER BY CASE WHEN w.word = :exactKeyword THEN 0 ELSE 1 END, w.word ASC")
    Page<Words> findByWordContainingOrderByExactMatchFirst(
            @Param("keyword") String keyword, 
            @Param("exactKeyword") String exactKeyword, 
            Pageable pageable);
    
    // 전체 검색 결과 개수 조회
    @Query("SELECT COUNT(w) FROM Words w WHERE w.word LIKE %:keyword%")
    long countByWordContaining(@Param("keyword") String keyword);
    
    // 용어명 중복 체크 (Primary Key이므로 existsById 사용 가능)
    boolean existsByWord(String word);
}