package com.superlawva.domain.user.entity;

// import com.superlawva.global.security.converter.AesCryptoConverter;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "users",
        indexes = {
                @Index(name = "idx_kakao_id", columnList = "kakaoId", unique = true),
                @Index(name = "idx_naver_id", columnList = "naverId", unique = true),
                @Index(name = "idx_email_hash", columnList = "emailHash", unique = true)
        }
)
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 소셜 로그인(카카오) 시에만 값 존재 */
    @Column(name = "kakao_id")
    private Long kakaoId;

    /** 소셜 로그인(네이버) 시에만 값 존재 */
    @Column(name = "naver_id")
    private String naverId;

    /** 해시된 이메일 (검색 및 중복 체크용) */
    @Column(name = "email_hash", nullable = false, unique = true)
    private String emailHash;

    /** 평문 이메일 (표시용, 암호화 저장) */
    @Column(name = "email", nullable = false)
    // @Convert(converter = AesCryptoConverter.class) // AES 암호화 임시 비활성화
    private String email;

    @Column(nullable = true)  // 소셜 로그인 사용자는 password가 null일 수 있음
    private String password;

    @Column(nullable = false)  // DB 스키마와 일치하도록 수정
    private String nickname;

    @Column(nullable = false)
    private String provider; // LOCAL, KAKAO, NAVER

    /** USER · ADMIN */
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Role role = Role.USER;
    public enum Role { USER, ADMIN }

    /** 생성-수정 시각 */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /** 이메일 인증 여부 */
    @Builder.Default
    @Column(name = "email_verified", nullable = false)
    private boolean emailVerified = false;

    /* ==================== JPA Life-cycle ==================== */

       @PrePersist
    protected void prePersist() {
        this.createdAt = this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    /* ==================== 도메인 로직 ==================== */

    public void changeEmail(String email) {
        this.email = email;
    }

    public void changePassword(String password) {
        this.password = password;
    }

    public void changeNickname(String nickname) {
        if (nickname != null) {
            this.nickname = nickname;
        }
    }

    public void updateSnsInfo(String nickname, String provider) {
        this.nickname = nickname;
        this.provider = provider;
    }
}

