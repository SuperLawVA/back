package com.superlawva.domain.log.entity;

import com.superlawva.domain.chatbot.entity.ChatMessageEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "chatbot_search_results")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SearchResult {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "search_id")
    private Long searchId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "msg_id", nullable = false)
    private ChatMessageEntity message;
    
    @Column(name = "search_query", length = 500)
    private String searchQuery;
    
    @Column(name = "search_type", length = 20)
    private String searchType;
    
    @Column(name = "doc_id", length = 100)
    private String docId;
    
    @Column(name = "doc_source", length = 200)
    private String docSource;
    
    @Column(name = "similarity_score", precision = 5, scale = 3)
    private BigDecimal similarityScore;
    
    @Column(name = "boosted_score", precision = 5, scale = 3)
    private BigDecimal boostedScore;
    
    @Column(name = "doc_metadata", columnDefinition = "JSON")
    private String docMetadata;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
} 