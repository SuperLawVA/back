package com.superlawva.domain.log.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.superlawva.domain.chatbot.entity.ChatMessageEntity;
import jakarta.persistence.*;
import jakarta.persistence.GenerationType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "search_logs")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SearchResult {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "search_id")
    private Long searchId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "msg_id", nullable = false)
    @JsonIgnore  // JSON 응답에서 메시지 정보 제외 (순환 참조 방지)
    private ChatMessageEntity message;
    
    @Column(name = "search_query", columnDefinition = "TEXT")
    private String searchQuery;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "search_type")
    private SearchType searchType;
    
    @Column(name = "doc_id", length = 100)
    private String docId;
    
    @Column(name = "doc_source", length = 50)
    private String docSource;
    
    @Column(name = "similarity_score", precision = 5, scale = 4)
    private BigDecimal similarityScore;
    
    @Column(name = "boosted_score", precision = 5, scale = 4)
    private BigDecimal boostedScore;
    
    @Column(name = "doc_metadata", columnDefinition = "JSON")
    private String docMetadata;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    /**
     * 검색 타입 enum (DB: 'law', 'case', 'both')
     * Java에서는 'case'가 예약어이므로 'case_'로 처리하고 toString()에서 변환
     */
    public enum SearchType {
        law,
        case_,  // DB에는 'case'로 저장됨
        both;
        
        @Override
        public String toString() {
            return this == case_ ? "case" : name();
        }
        
        /**
         * DB 값으로부터 enum 값 생성
         */
        public static SearchType fromDbValue(String dbValue) {
            return switch (dbValue) {
                case "law" -> law;
                case "case" -> case_;
                case "both" -> both;
                default -> throw new IllegalArgumentException("Unknown search type: " + dbValue);
            };
        }
    }
    
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
} 