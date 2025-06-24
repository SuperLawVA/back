package com.superlawva.global.security.service;

import com.superlawva.global.security.util.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * JWT 토큰 블랙리스트 관리 서비스
 * Redis를 사용하여 무효화된 토큰을 관리합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    private final RedisTemplate<String, String> redisTemplate;
    private final JwtTokenProvider jwtTokenProvider;
    
    private static final String BLACKLIST_PREFIX = "blacklist:";

    /**
     * 토큰을 블랙리스트에 추가
     * @param token
     */
    public void addTokenToBlacklist(String token) {
        // Implementation of adding token to blacklist
    }
} 