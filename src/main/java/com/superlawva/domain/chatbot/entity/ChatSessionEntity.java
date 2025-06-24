package com.superlawva.domain.chatbot.entity;

import com.superlawva.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "chatbot_sessions")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatSessionEntity {
    
    @Id
    @Column(name = "session_id", length = 36)
    private String sessionId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "last_active_at")
    private LocalDateTime lastActiveAt;
    
    @Column(name = "last_question_type", length = 50)
    private String lastQuestionType;
    
    @Column(name = "total_messages")
    @Builder.Default
    private Integer totalMessages = 0;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    @Builder.Default
    private SessionStatus status = SessionStatus.active;
    
    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL)
    @Builder.Default
    private List<ChatMessageEntity> messages = new ArrayList<>();
    
    public enum SessionStatus {
        active, closed, expired
    }
    
    @PrePersist
    protected void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.lastActiveAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void preUpdate() {
        this.lastActiveAt = LocalDateTime.now();
    }
    
    public void updateActivity(String questionType) {
        this.lastQuestionType = questionType;
        this.lastActiveAt = LocalDateTime.now();
        this.totalMessages++;
    }
    
    public void closeSession() {
        this.status = SessionStatus.closed;
        this.lastActiveAt = LocalDateTime.now();
    }
} 