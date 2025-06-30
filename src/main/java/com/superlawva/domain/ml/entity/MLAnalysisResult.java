package com.superlawva.domain.ml.entity;

import jakarta.persistence.*;
import jakarta.persistence.GenerationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import com.superlawva.global.security.converter.AesCryptoConverter;
import com.superlawva.domain.ml.entity.CertificateEntity;
import java.time.LocalDateTime;
import lombok.experimental.FieldNameConstants;

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

    @Convert(converter = AesCryptoConverter.class)
    @Lob
    @Column(name = "analysis_content", columnDefinition = "LONGTEXT")
    private String analysisContent; // 분석 결과 내용

    @Convert(converter = AesCryptoConverter.class)
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

    /* ==================== 연관 문서 (선택) ==================== */
    @Transient
    private CertificateEntity certificate;

    public CertificateEntity getCertificate() {
        return certificate;
    }

    public void setCertificate(CertificateEntity certificate) {
        this.certificate = certificate;
    }

    /* ==================== 추가 메타 데이터 ==================== */

    @Lob
    @Column(name = "request_data", columnDefinition = "LONGTEXT")
    private String requestData;

    @Lob
    @Column(name = "generation_metadata", columnDefinition = "LONGTEXT")
    private String generationMetadata;

    @Column(name = "model_name")
    private String modelName;

    @Column(name = "model_version")
    private String modelVersion;

    @Column(name = "generation_time_seconds")
    private Double generationTimeSeconds;

    @Column(name = "token_count")
    private Integer tokenCount;

    @Column(name = "quality_score")
    private Integer qualityScore;

    @Enumerated(EnumType.STRING)
    @Column(name = "analysis_status", length = 20)
    @Builder.Default
    private AnalysisStatus analysisStatus = AnalysisStatus.GENERATED;

    public enum AnalysisStatus {
        GENERATED,
        PROCESSING,
        COMPLETED,
        FAILED
    }
}
