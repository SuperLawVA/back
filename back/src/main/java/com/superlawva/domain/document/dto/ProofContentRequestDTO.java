package com.superlawva.domain.document.dto;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProofContentRequestDTO {
    
    @NotNull(message = "사용자 ID는 필수입니다")
    private Long userId;
    
    @NotBlank(message = "증명서 유형은 필수입니다")
    private String proofType; // "거주확인서", "계약확인서", "임대차현황확인서" 등
    
    // 증명 목적
    @NotBlank(message = "증명 목적은 필수입니다")  
    private String purpose; // "관공서 제출용", "회사 제출용", "금융기관 제출용" 등
    
    // 발행 정보
    private LocalDate issueDate; // 발행일 (기본값: 오늘)
    
    private String issuerName; // 발행자 명
    
    private String issuerTitle; // 발행자 직책
    
    // 증명 대상 기간
    private LocalDate periodStartDate; // 증명 시작 기간
    
    private LocalDate periodEndDate; // 증명 종료 기간
    
    // 특별 요구사항
    private List<String> requiredFields; // 포함되어야 할 특정 정보들
    
    private String additionalRequirements; // 추가 요구사항
    
    // 문서 형식 옵션
    @Builder.Default
    private String documentFormat = "PDF"; // "PDF", "DOCX"
    
    @Builder.Default
    private boolean includeOfficialSeal = true; // 공인 도장 포함 여부
    
    @Builder.Default  
    private boolean includeContactInfo = true; // 연락처 정보 포함 여부
    
    // 언어 설정
    @Builder.Default
    private String language = "KO"; // "KO", "EN"
} 