package com.superlawva.domain.log.entity;

import jakarta.persistence.*;
import jakarta.persistence.GenerationType;
import lombok.*;

import java.time.LocalDateTime;

@Entity @Table(name = "page_views")
@Getter @Setter @NoArgsConstructor
public class PageView {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String path;
    private LocalDateTime startedAt;
    private Integer duration;            // ms

    @ManyToOne(fetch = FetchType.LAZY)
    private Session session;
}
