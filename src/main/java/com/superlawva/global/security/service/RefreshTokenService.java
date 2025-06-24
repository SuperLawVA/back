package com.superlawva.global.security.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * Refresh Token 관리 서비스
 * Redis를 사용하여 refresh token을 저장하고 관리합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RedisTemplate<String, String> redisTemplate;
    
    private static final String REFRESH_TOKEN_PREFIX = "refresh_token:";
    private static final Duration REFRESH_TOKEN_VALIDITY = Duration.ofDays(14); // 2주

    /**
     * Refresh Token 저장
     * @param userId 사용자 ID
     * @param refreshToken Refresh Token
     */
    public void saveRefreshToken(Long userId, String refreshToken) {
        try {
            String key = REFRESH_TOKEN_PREFIX + userId;
            redisTemplate.opsForValue().set(key, refreshToken, REFRESH_TOKEN_VALIDITY);
            log.debug("RefreshToken 저장됨: userId={}", userId);
        } catch (Exception e) {
            log.error("RefreshToken 저장 실패: userId={}, error={}", userId, e.getMessage());
        }
    }

    /**
     * Refresh Token 조회
     * @param userId 사용자 ID
     * @return Refresh Token 또는 null
     */
    public String getRefreshToken(Long userId) {
        try {
            String key = REFRESH_TOKEN_PREFIX + userId;
            return redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            log.error("RefreshToken 조회 실패: userId={}, error={}", userId, e.getMessage());
            return null;
        }
    }

    /**
     * Refresh Token 삭제 (로그아웃 시)
     * @param userId 사용자 ID
     */
    public void deleteRefreshToken(Long userId) {
        try {
            String key = REFRESH_TOKEN_PREFIX + userId;
            Boolean deleted = redisTemplate.delete(key);
            log.info("RefreshToken 삭제됨: userId={}, deleted={}", userId, deleted);
        } catch (Exception e) {
            log.error("RefreshToken 삭제 실패: userId={}, error={}", userId, e.getMessage());
        }
    }

    /**
     * Refresh Token 존재 여부 확인
     * @param userId 사용자 ID
     * @return 존재하면 true
     */
    public boolean existsRefreshToken(Long userId) {
        try {
            String key = REFRESH_TOKEN_PREFIX + userId;
            return Boolean.TRUE.equals(redisTemplate.hasKey(key));
        } catch (Exception e) {
            log.error("RefreshToken 존재 확인 실패: userId={}, error={}", userId, e.getMessage());
            return false;
        }
    }
} 