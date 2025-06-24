package com.superlawva.domain.ocr3.repository;

import com.superlawva.domain.ocr3.entity.ContractData;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContractDataRepository extends MongoRepository<ContractData, String> {
    
    // Find contracts by user ID
    List<ContractData> findByUserId(String userId);
    
    // Find contracts by contract type
    List<ContractData> findByContractType(String contractType);
    
    // Find contracts by lessor name
    List<ContractData> findByLessorName(String lessorName);
    
    // Find contracts by lessee name
    List<ContractData> findByLesseeName(String lesseeName);
    
    // Find by ID and user ID (for security)
    Optional<ContractData> findByIdAndUserId(String id, String userId);
}