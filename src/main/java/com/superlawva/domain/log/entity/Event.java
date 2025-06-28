package com.superlawva.domain.log.entity;

import com.superlawva.domain.user.entity.User;
import jakarta.persistence.*;
import jakarta.persistence.GenerationType;
import lombok.*;

import java.time.LocalDateTime;

@Entity @Table(name = "events")
@Getter @Setter @NoArgsConstructor
public class Event {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String type;                 // click, hover …
    private String target;
    private LocalDateTime time;

    @ManyToOne(fetch = FetchType.LAZY) private Session  session;
    @ManyToOne(fetch = FetchType.LAZY) private PageView view;
    @ManyToOne(fetch = FetchType.LAZY) private User     user;
}
