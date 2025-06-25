package com.superlawva.domain.search.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "cases")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cases {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(name = "case_id")
    private String caseId;
    
    @Column(name = "case_number")
    private String caseNumber;
    
    @Column(name = "filing_year")
    private Integer filingYear;
    
    @Column(name = "case_type")
    private String caseType;
    
    @Column(name = "filing_number")
    private Integer filingNumber;
    
    @Column(name = "case_title")
    private String caseTitle;
    
    @Column(name = "judgement_order", columnDefinition = "TEXT")
    private String judgementOrder;
    
    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;
    
    @Column(name = "claim", columnDefinition = "TEXT")
    private String claim;
    
    @Column(name = "decision_date")
    private LocalDate decisionDate;
} 