package com.superlawva.domain.log.entity;

import com.superlawva.domain.chatbot.entity.ChatMessageEntity;
import jakarta.persistence.*;
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
    
    public enum SearchType {
        law, case_, both;
        
        @Override
        public String toString() {
            return this == case_ ? "case" : name();
        }
    }
    
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
} 