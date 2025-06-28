package com.superlawva.domain.ocr3.repository;

import com.superlawva.domain.ocr3.entity.ContractData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContractDataRepository extends JpaRepository<ContractData, Long> {

    // Find contracts by user ID
    List<ContractData> findByUserId(String userId);

    // Find contracts by contract type
    List<ContractData> findByContractType(String contractType);

    // Find contracts by lessor name (embedded field 접근)
    @Query("SELECT c FROM ContractData c WHERE c.lessor.name = :lessorName")
    List<ContractData> findByLessorName(@Param("lessorName") String lessorName);

    // Find contracts by lessee name (embedded field 접근)
    @Query("SELECT c FROM ContractData c WHERE c.lessee.name = :lesseeName")
    List<ContractData> findByLesseeName(@Param("lesseeName") String lesseeName);

    // Find by ID and user ID (for security)
    Optional<ContractData> findByIdAndUserId(Long id, String userId);

    // 생성 상태별 조회
    List<ContractData> findByIsGenerated(Boolean isGenerated);

    // 사용자별 최신 순 조회
    List<ContractData> findByUserIdOrderByCreatedDateDesc(String userId);

    // 계약 타입과 사용자별 조회
    List<ContractData> findByContractTypeAndUserId(String contractType, String userId);
}