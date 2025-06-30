package com.superlawva.domain.alarm.entity;

import com.superlawva.domain.alarm.entity.AlarmType;
import jakarta.persistence.*;
import jakarta.persistence.GenerationType;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import com.superlawva.global.security.converter.AesCryptoConverter;

import java.time.LocalDateTime;

@Entity
@Table(name = "alarms")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlarmEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long alarmId;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "contract_id")
    private String contractId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "alarm_type", nullable = false)
    private AlarmType alarmType;
    
    @Convert(converter = AesCryptoConverter.class)
    @Column(name = "note", length = 500)
    private String note;
    
    @Convert(converter = AesCryptoConverter.class)
    @Column(name = "extra_info", columnDefinition = "TEXT")
    private String extraInfo;
    
    @Column(name = "is_read", nullable = false)
    @Builder.Default
    private Boolean isRead = false;
    
    @Column(name = "alarm_date", nullable = false)
    private LocalDateTime alarmDate;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
    
    @Column(name = "is_sent")
    @Builder.Default
    private boolean isSent = false;
    
    @Column(name = "sent_at")
    private LocalDateTime sentAt;
    
    // isRead 필드의 getter/setter 메서드
    public Boolean getIsRead() {
        return isRead;
    }
    
    public void setIsRead(Boolean isRead) {
        this.isRead = isRead;
    }
    
    public boolean isRead() {
        return isRead != null ? isRead : false;
    }
    
    public void setRead(boolean read) {
        this.isRead = read;
    }
} 