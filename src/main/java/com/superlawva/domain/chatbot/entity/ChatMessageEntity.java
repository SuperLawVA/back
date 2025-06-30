package com.superlawva.domain.chatbot.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.persistence.GenerationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import com.superlawva.global.security.converter.AesCryptoConverter;

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
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id")
    @JsonIgnore  // JSON 응답에서 세션 정보 제외 (순환 참조 방지)
    private ChatSessionEntity session;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private MessageRole role;
    
    @Convert(converter = AesCryptoConverter.class)
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
    
    // 명시적으로 필요한 getter 메서드들 추가
    public String getContent() {
        return content;
    }
    
    public String getQuestionType() {
        return questionType;
    }
    
    public BigDecimal getResponseTimeSeconds() {
        return responseTimeSeconds;
    }
    
    public ChatSessionEntity getSession() {
        return session;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    // Builder 메서드 추가
    public static ChatMessageEntityBuilder builder() {
        return new ChatMessageEntityBuilder();
    }
    
    public static class ChatMessageEntityBuilder {
        private Long id;
        private ChatSessionEntity session;
        private MessageRole role;
        private String content;
        private String questionType;
        private BigDecimal responseTimeSeconds;
        private String tokenUsage;
        private LocalDateTime createdAt;
        
        public ChatMessageEntityBuilder id(Long id) {
            this.id = id;
            return this;
        }
        
        public ChatMessageEntityBuilder session(ChatSessionEntity session) {
            this.session = session;
            return this;
        }
        
        public ChatMessageEntityBuilder role(MessageRole role) {
            this.role = role;
            return this;
        }
        
        public ChatMessageEntityBuilder content(String content) {
            this.content = content;
            return this;
        }
        
        public ChatMessageEntityBuilder questionType(String questionType) {
            this.questionType = questionType;
            return this;
        }
        
        public ChatMessageEntityBuilder responseTimeSeconds(BigDecimal responseTimeSeconds) {
            this.responseTimeSeconds = responseTimeSeconds;
            return this;
        }
        
        public ChatMessageEntityBuilder tokenUsage(String tokenUsage) {
            this.tokenUsage = tokenUsage;
            return this;
        }
        
        public ChatMessageEntityBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }
        
        public ChatMessageEntity build() {
            return new ChatMessageEntity(id, session, role, content, questionType, responseTimeSeconds, tokenUsage, createdAt);
        }
    }
} 