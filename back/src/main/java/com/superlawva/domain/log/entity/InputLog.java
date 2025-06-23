package com.superlawva.domain.log.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "input_log")
@Getter @Setter @NoArgsConstructor
public class InputLog {

    @Id
    private Long id;

    private String fieldName;
    private String action;               // focus / blur / change …

    @MapsId
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id")
    private Event event;
}
