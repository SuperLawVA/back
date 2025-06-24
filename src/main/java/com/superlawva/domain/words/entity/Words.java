// Words.java
package com.superlawva.domain.words.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "words")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Words {
    
    @Id
    @Column(name = "word", length = 255, nullable = false)
    private String word;  // Primary Key로 사용되는 용어명
    
    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;  // 용어 설명
}