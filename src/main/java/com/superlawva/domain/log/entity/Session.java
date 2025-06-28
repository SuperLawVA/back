package com.superlawva.domain.log.entity;

import com.superlawva.domain.user.entity.User;
import jakarta.persistence.*;
import jakarta.persistence.GenerationType;
import lombok.*;

import java.time.LocalDateTime;

@Entity @Table(name = "sessions")
@Getter @Setter @NoArgsConstructor
public class Session {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime startedAt;
    private LocalDateTime endAt;

    @Enumerated(EnumType.STRING)
    private Device device;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;
}
