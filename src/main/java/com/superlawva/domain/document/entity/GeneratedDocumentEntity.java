package com.superlawva.domain.document.entity;

import jakarta.persistence.*;
import jakarta.persistence.GenerationType;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "generated_documents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GeneratedDocumentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "document_id")
    private Long documentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "generation_type")
    private DocGenerationType generationType;

    @Lob
    @Column(name = "request_data", columnDefinition = "TEXT")
    private String requestData;

    @Lob
    @Column(name = "generation_metadata", columnDefinition = "TEXT")
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

    @Column(name = "user_rating")
    private Integer userRating;

    @Column(name = "user_feedback")
    private String userFeedback;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private DocumentStatus status;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Lob
    @Column(name = "additional_metadata_json", columnDefinition = "TEXT")
    private String additionalMetadataJson;

    public enum DocGenerationType {
        CONTRACT_GENERATION, PROOF_CONTENT, DOCUMENT_MODIFICATION, TEMPLATE_BASED
    }

    public enum DocumentStatus {
        GENERATING, GENERATED, FAILED, REVIEWED, APPROVED
    }
}
