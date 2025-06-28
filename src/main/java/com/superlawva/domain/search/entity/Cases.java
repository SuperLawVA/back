package com.superlawva.domain.search.entity;

import jakarta.persistence.*;
import jakarta.persistence.GenerationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "cases", indexes = {
    @Index(name = "idx_case_number", columnList = "case_number"),
    @Index(name = "idx_case_id", columnList = "case_id"),
    @Index(name = "idx_case_type", columnList = "case_type"),
    @Index(name = "idx_decision_date", columnList = "decision_date")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cases {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(name = "case_id", unique = true)
    @Size(max = 50, message = "사건 ID는 50자 이하여야 합니다.")
    private String caseId;
    
    @Column(name = "case_number")
    @NotBlank(message = "사건번호는 필수입니다.")
    @Size(max = 100, message = "사건번호는 100자 이하여야 합니다.")
    private String caseNumber;
    
    @Column(name = "filing_year")
    private Integer filingYear;
    
    @Column(name = "case_type")
    @Size(max = 50, message = "사건유형은 50자 이하여야 합니다.")
    private String caseType;
    
    @Column(name = "filing_number")
    private Integer filingNumber;
    
    @Column(name = "case_title")
    @Size(max = 500, message = "사건명은 500자 이하여야 합니다.")
    private String caseTitle;
    
    @Column(name = "judgement_order", columnDefinition = "TEXT")
    private String judgementOrder;
    
    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;
    
    @Column(name = "claim", columnDefinition = "TEXT")
    private String claim;
    
    @Column(name = "decision_date")
    private LocalDate decisionDate;
    
    /**
     * 사건번호와 사건명을 조합한 전체 제목 반환
     */
    public String getFullTitle() {
        return String.format("%s - %s", 
            this.caseNumber != null ? this.caseNumber : "사건번호 미상",
            this.caseTitle != null ? this.caseTitle : "사건명 미상"
        );
    }
    
    /**
     * 사건 요약 정보 반환
     */
    public String getSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("사건번호: ").append(this.caseNumber).append("\n");
        summary.append("사건유형: ").append(this.caseType).append("\n");
        summary.append("판결일: ").append(this.decisionDate).append("\n");
        return summary.toString();
    }
} 