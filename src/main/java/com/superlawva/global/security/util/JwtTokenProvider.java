package com.superlawva.global.security.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access-token-validity:43200000}") // 기본 12시간
    private long validityMs;

    private SecretKey key;

    @PostConstruct
    public void initialize() {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("❌ jwt.secret is missing. Check environment variables or .env file.");
        }
        
        try {
            // Base64로 인코딩된 키 디코딩 시도
            byte[] keyBytes = java.util.Base64.getDecoder().decode(secret);
            if (keyBytes.length >= 32) { // 최소 256비트
                this.key = Keys.hmacShaKeyFor(keyBytes);
                return;
            }
        } catch (IllegalArgumentException e) {
            // Base64가 아닌 경우 문자열로 처리
        }
        
        // 문자열 키를 바이트로 변환 (최소 32바이트 보장)
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            // 키가 32바이트보다 짧으면 패딩
            byte[] paddedKey = new byte[32];
            System.arraycopy(keyBytes, 0, paddedKey, 0, keyBytes.length);
            this.key = Keys.hmacShaKeyFor(paddedKey);
        } else {
            this.key = Keys.hmacShaKeyFor(keyBytes);
        }
    }

    public String createToken(String email, Long userId) {
        Claims claims = Jwts.claims().setSubject(email);
        claims.put("userId", userId);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + validityMs))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String getSubject(String token) {
        return Jwts.parser()
                .setSigningKey(key)
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public Long getUserId(String token) {
        return Jwts.parser()
                .setSigningKey(key)
                .parseClaimsJws(token)
                .getBody()
                .get("userId", Long.class);
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .setSigningKey(key)
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public boolean validate(String token) {
        return validateToken(token);
    }

    public String getEmail(String token) {
        return getSubject(token);
    }

    public String resolveToken(jakarta.servlet.http.HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    public String createToken(String email) {
        Claims claims = Jwts.claims().setSubject(email);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + validityMs))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 소셜 로그인 임시 토큰 생성 (이메일 정보가 없을 때 사용)
     * @param socialId 소셜 플랫폼 사용자 ID
     * @param provider 소셜 플랫폼 (KAKAO, NAVER)
     * @param nickname 닉네임
     * @return 임시 토큰 (짧은 유효기간)
     */
    public String createTempToken(String socialId, String provider, String nickname) {
        Claims claims = Jwts.claims().setSubject("TEMP");
        claims.put("socialId", socialId);
        claims.put("provider", provider);
        claims.put("nickname", nickname);
        claims.put("type", "TEMP");

        // 임시 토큰은 30분만 유효
        long tempValidityMs = 30 * 60 * 1000; // 30분

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + tempValidityMs))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 임시 토큰에서 소셜 정보 추출
     * @param tempToken 임시 토큰
     * @return 소셜 정보 맵
     */
    public Claims getTempTokenClaims(String tempToken) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(key)
                    .parseClaimsJws(tempToken)
                    .getBody();
            
            // 임시 토큰인지 확인
            if (!"TEMP".equals(claims.getSubject()) || !"TEMP".equals(claims.get("type"))) {
                throw new IllegalArgumentException("유효하지 않은 임시 토큰입니다.");
            }
            
            return claims;
        } catch (JwtException | IllegalArgumentException e) {
            throw new IllegalArgumentException("임시 토큰이 유효하지 않거나 만료되었습니다.");
        }
    }

    /**
     * 쿠키나 헤더에서 JWT 토큰 추출 (선택사항 - 사용하지 않을 수도 있음)
     */
    public String getJwtFromRequest(jakarta.servlet.http.HttpServletRequest request) {
        // 1. Authorization 헤더에서 먼저 확인
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        
        // 2. 쿠키에서 확인 (필요시)
        jakarta.servlet.http.Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (jakarta.servlet.http.Cookie cookie : cookies) {
                if ("access-token".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        
        return null;
    }

    /**
     * 토큰의 만료 시간을 가져옵니다
     */
    public long getExpirationTime(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getExpiration()
                .getTime();
    }
}
