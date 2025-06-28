package com.superlawva.domain.user.entity;

import jakarta.persistence.*;
import jakarta.persistence.GenerationType;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;

@Entity
@Table(
        name = "users",
        indexes = {
                @Index(name = "idx_kakao_id", columnList = "kakao_id", unique = true),
                @Index(name = "idx_naver_id", columnList = "naver_id", unique = true),
                @Index(name = "idx_email", columnList = "email", unique = true),
                @Index(name = "idx_provider", columnList = "provider")
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 소셜 로그인(카카오) 시에만 값 존재 */
    @Column(name = "kakao_id")
    private Long kakaoId;

    /** 소셜 로그인(네이버) 시에만 값 존재 */
    @Column(name = "naver_id")
    private String naverId;

    /** 이메일 (로그인 및 중복 체크용) */
    @Column(name = "email", nullable = false, unique = true)
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

    /** 생성-수정 시각 */
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /** 이메일 인증 여부 */
    @Builder.Default
    @Column(name = "email_verified", nullable = false)
    private boolean emailVerified = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public enum Role { USER, ADMIN }

    // UserDetails 구현
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public String getPassword() {
        return password != null ? password : "";
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return deletedAt == null;
    }

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

    // 명시적으로 getId() 메서드 추가
    public Long getId() {
        return this.id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
}

