package com.superlawva.domain.search.repository;

import com.superlawva.domain.search.entity.Cases;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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
} 