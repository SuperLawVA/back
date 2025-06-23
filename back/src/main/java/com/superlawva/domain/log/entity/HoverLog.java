package com.superlawva.domain.log.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "hover_log")
@Getter @Setter @NoArgsConstructor
public class HoverLog {

    @Id
    private Long id;                     // Event PK 그대로 사용

    private String element;
    private Integer duration;            // milliseconds

    @MapsId
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id")
    private Event event;
}