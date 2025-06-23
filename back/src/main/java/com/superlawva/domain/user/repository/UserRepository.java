package com.superlawva.domain.user.repository;

import java.util.Optional;
import com.superlawva.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {

    // 🔵 기존 소셜 로그인용 메서드들
    Optional<User> findByKakaoId(Long kakaoId);
    Optional<User> findByNaverId(String naverId);
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    // 🟢 새로 추가: 일반 로그인용 메서드들
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);

    // 🟢 새로 추가: 이메일 인증용
    Optional<User> findByEmailAndVerificationCode(String email, String verificationCode);

    // 🟢 새로 추가: 통합 로그인 지원 (이메일 또는 사용자명으로 찾기)
    @Query("SELECT u FROM User u WHERE u.email = :emailOrUsername OR u.username = :emailOrUsername")
    Optional<User> findByEmailOrUsername(@Param("emailOrUsername") String emailOrUsername);

    // 🟢 새로 추가: 중복 검사 (이메일과 사용자명 모두) - 구현 수정
    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM User u WHERE u.email = :email OR u.username = :username")
    boolean existsByEmailOrUsername(@Param("email") String email, @Param("username") String username);

    // 🟢 새로 추가: 활성 사용자 조회 (이메일 인증된 사용자만)
    @Query("SELECT u FROM User u WHERE u.emailVerified = true")
    java.util.List<User> findAllVerifiedUsers();

    // 🟢 새로 추가: 로그인 타입별 사용자 조회
    java.util.List<User> findByLoginType(User.LoginType loginType);

    // 🟢 새로 추가: 만료된 인증 코드를 가진 사용자 조회 (정리용)
    @Query("SELECT u FROM User u WHERE u.verificationCodeExpiry < CURRENT_TIMESTAMP AND u.emailVerified = false")
    java.util.List<User> findUsersWithExpiredVerificationCodes();
}