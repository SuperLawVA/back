package com.superlawva.domain.ml.entity;

import jakarta.persistence.*;
import jakarta.persistence.GenerationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "ml_analysis_results")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MLAnalysisResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "contract_id", nullable = false)
    private String contractId;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "analysis_type", nullable = false, length = 50)
    private String analysisType; // CONTRACT_ANALYSIS, PROOF_GENERATION 등

    @Column(name = "status", nullable = false, length = 20)
    private String status; // SUCCESS, FAILED, PROCESSING

    @Column(name = "raw_ml_response_id")
    private Integer rawMlResponseId; // ML API 응답 ID

    @Lob
    @Column(name = "analysis_content", columnDefinition = "LONGTEXT")
    private String analysisContent; // 분석 결과 내용

    @Lob
    @Column(name = "raw_response", columnDefinition = "LONGTEXT")
    private String rawResponse; // 원본 ML 응답 (JSON 문자열)

    @Column(name = "risk_score")
    private Double riskScore;

    @Column(name = "risk_level", length = 20)
    private String riskLevel; // LOW, MEDIUM, HIGH, CRITICAL

    @Column(name = "processing_time_seconds")
    private Double processingTimeSeconds;

    @CreationTimestamp
    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @UpdateTimestamp
    @Column(name = "updated_date")
    private LocalDateTime updatedDate;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "updated_by")
    private String updatedBy;
}
