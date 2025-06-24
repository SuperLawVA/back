package com.superlawva.domain.document.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.DBRef;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

@Document(collection = "generated_documents")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GeneratedDocument {
    
    @Id
    private String id; // MongoDB ObjectId
    
    @Field("user_id")
    private Long userId;
    
    // 기본 Document와 연관관계 (참조 방식)
    @Field("document_id")
    private String documentId; // Document의 ObjectId 참조
    
    @Field("generation_type")
    private GenerationType generationType;
    
    // 생성 요청 정보 (JSON 형태로 저장)
    @Field("request_data")
    private String requestData;
    
    // 생성 결과 메타데이터
    @Field("generation_metadata")
    private String generationMetadata;
    
    // AI 모델 정보
    @Field("model_name")
    private String modelName;
    
    @Field("model_version")
    private String modelVersion;
    
    // 생성 성능 정보
    @Field("generation_time_seconds")
    private Double generationTimeSeconds;
    
    @Field("token_count")
    private Integer tokenCount;
    
    // 품질 점수 (1-100)
    @Field("quality_score")
    private Integer qualityScore;
    
    // 사용자 피드백
    @Field("user_rating")
    private Integer userRating; // 1-5 점
    
    @Field("user_feedback")
    private String userFeedback;
    
    @Field("status")
    @Builder.Default
    private DocumentStatus status = DocumentStatus.GENERATED;
    
    @CreatedDate
    @Field("created_at")
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Field("updated_at")
    private LocalDateTime updatedAt;
    
    // 추가 메타데이터 (유연한 구조)
    @Field("additional_metadata")
    private java.util.Map<String, Object> additionalMetadata;
    
    // 생성 타입 열거형
    public enum GenerationType {
        CONTRACT_GENERATION("계약서 생성"),
        PROOF_CONTENT("증명 내용 생성"),
        DOCUMENT_MODIFICATION("문서 수정"),
        TEMPLATE_BASED("템플릿 기반 생성");
        
        private final String description;
        
        GenerationType(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    // 문서 상태 열거형
    public enum DocumentStatus {
        GENERATING("생성 중"),
        GENERATED("생성 완료"),
        FAILED("생성 실패"),
        REVIEWED("검토 완료"),
        APPROVED("승인 완료");
        
        private final String description;
        
        DocumentStatus(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
} 