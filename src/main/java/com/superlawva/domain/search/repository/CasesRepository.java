package com.superlawva.domain.search.repository;

import com.superlawva.domain.search.entity.Cases;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface CasesRepository extends JpaRepository<Cases, Integer> {
    
    /**
     * caseId로 판례 조회
     */
    Optional<Cases> findByCaseId(String caseId);
    
    /**
     * 여러 caseId로 판례들 조회
     */
    List<Cases> findByCaseIdIn(List<String> caseIds);
    
    /**
     * 사건번호로 판례 조회
     */
    Optional<Cases> findByCaseNumber(String caseNumber);
    
    /**
     * 제목으로 판례 검색 (부분 일치)
     */
    List<Cases> findByCaseTitleContainingIgnoreCase(String title);
    
    /**
     * 접수년도로 판례 조회
     */
    List<Cases> findByFilingYear(Integer filingYear);
    
    /**
     * 사건 유형으로 판례 조회
     */
    List<Cases> findByCaseType(String caseType);
    
    /**
     * 판결일 범위로 판례 조회
     */
    List<Cases> findByDecisionDateBetween(LocalDate startDate, LocalDate endDate);
    
    /**
     * 최근 판례 조회 (페이징)
     */
    Page<Cases> findByDecisionDateIsNotNullOrderByDecisionDateDesc(Pageable pageable);
    
    /**
     * 키워드로 전문 검색 (제목, 판결문, 사유 통합)
     */
    @Query("SELECT c FROM Cases c WHERE " +
           "LOWER(c.caseTitle) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(c.judgementOrder) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(c.reason) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Cases> searchByKeyword(@Param("keyword") String keyword);
    
    /**
     * 사건 유형별 통계
     */
    @Query("SELECT c.caseType, COUNT(c) FROM Cases c GROUP BY c.caseType ORDER BY COUNT(c) DESC")
    List<Object[]> getCaseTypeStatistics();
    
    /**
     * 연도별 판례 수 통계
     */
    @Query("SELECT c.filingYear, COUNT(c) FROM Cases c WHERE c.filingYear IS NOT NULL GROUP BY c.filingYear ORDER BY c.filingYear DESC")
    List<Object[]> getYearlyStatistics();
    
    /**
     * 특정 사건 유형의 최근 판례들
     */
    @Query("SELECT c FROM Cases c WHERE c.caseType = :caseType AND c.decisionDate IS NOT NULL ORDER BY c.decisionDate DESC")
    List<Cases> findRecentCasesByType(@Param("caseType") String caseType, Pageable pageable);
} 