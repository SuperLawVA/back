package com.superlawva.domain.chatbot.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "chatbot_messages")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "msg_id")
    private Long msgId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id")
    private ChatSessionEntity session;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private MessageRole role;
    
    @Column(name = "content", columnDefinition = "TEXT")
    private String content;
    
    @Column(name = "question_type", length = 50)
    private String questionType;
    
    @Column(name = "response_time_seconds", precision = 10, scale = 3)
    private BigDecimal responseTimeSeconds;
    
    @Column(name = "token_usage", columnDefinition = "JSON")
    private String tokenUsage;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    public enum MessageRole {
        user, assistant
    }
    
    @PrePersist
    protected void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
    
    public static ChatMessageEntity createUserMessage(ChatSessionEntity session, String content) {
        return ChatMessageEntity.builder()
                .session(session)
                .role(MessageRole.user)
                .content(content)
                .build();
    }
    
    public static ChatMessageEntity createAssistantMessage(
            ChatSessionEntity session, 
            String content, 
            String questionType, 
            BigDecimal responseTimeSeconds) {
        return ChatMessageEntity.builder()
                .session(session)
                .role(MessageRole.assistant)
                .content(content)
                .questionType(questionType)
                .responseTimeSeconds(responseTimeSeconds)
                .build();
    }
} 