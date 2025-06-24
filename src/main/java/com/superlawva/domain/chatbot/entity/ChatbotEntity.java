package com.superlawva.domain.chatbot.entity;

import com.superlawva.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "chatbot_conversations")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatbotEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    
    @Column(name = "session_id", length = 100)
    private String sessionId;
    
    @Column(name = "user_message", columnDefinition = "TEXT")
    private String userMessage;
    
    @Column(name = "bot_response", columnDefinition = "TEXT")
    private String botResponse;
    
    @Column(name = "processing_time")
    private Long processingTime;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ChatStatus status;
    
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    public enum ChatStatus {
        SUCCESS, ERROR, PENDING
    }
    
    public void updateResponse(String botResponse, Long processingTime, ChatStatus status) {
        this.botResponse = botResponse;
        this.processingTime = processingTime;
        this.status = status;
    }
} 