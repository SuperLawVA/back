package com.superlawva.global.security.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * AES 양방향 암호화 유틸리티
 * 민감한 데이터(개인정보, 토큰 등)를 안전하게 저장하기 위해 사용
 */
@Component
public class AESUtil {

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/ECB/PKCS5Padding";

    @Value("${aes.secret-key}")
    private String secretKey;

    /**
     * 텍스트를 AES로 암호화
     * @param plainText 평문
     * @return Base64 인코딩된 암호문
     */
    public String encrypt(String plainText) {
        try {
            // null 체크 추가
            if (plainText == null) {
                plainText = "";
            }

            SecretKeySpec secretKeySpec = createSecretKey();
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);

            byte[] encryptedBytes = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            throw new RuntimeException("암호화 실패: " + e.getMessage(), e);
        }
    }

    /**
     * AES로 암호화된 텍스트를 복호화
     * @param encryptedText Base64 인코딩된 암호문
     * @return 평문
     */
    public String decrypt(String encryptedText) {
        try {
            // null 체크 추가
            if (encryptedText == null || encryptedText.trim().isEmpty()) {
                return "";
            }

            SecretKeySpec secretKeySpec = createSecretKey();
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);

            byte[] decodedBytes = Base64.getDecoder().decode(encryptedText);
            byte[] decryptedBytes = cipher.doFinal(decodedBytes);
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("복호화 실패: " + e.getMessage(), e);
        }
    }

    /**
     * 보안 토큰 생성 (세션 ID, 인증 토큰 등에 사용)
     * @param data 토큰에 포함할 데이터
     * @return 암호화된 토큰
     */
    public String generateSecureToken(String data) {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String combined = data + ":" + timestamp;
        return encrypt(combined);
    }

    /**
     * 보안 토큰에서 데이터 추출
     * @param token 암호화된 토큰
     * @return 원본 데이터
     */
    public String extractDataFromToken(String token) {
        String decrypted = decrypt(token);
        String[] parts = decrypted.split(":");
        return parts.length > 0 ? parts[0] : null;
    }

    /**
     * 토큰 유효성 검증 (타임스탬프 기반)
     * @param token 검증할 토큰
     * @param validityMs 유효 시간 (밀리초)
     * @return 유효 여부
     */
    public boolean isTokenValid(String token, long validityMs) {
        try {
            String decrypted = decrypt(token);
            String[] parts = decrypted.split(":");
            if (parts.length < 2) return false;

            long timestamp = Long.parseLong(parts[1]);
            long now = System.currentTimeMillis();
            return (now - timestamp) <= validityMs;
        } catch (Exception e) {
            return false;
        }
    }

    private SecretKeySpec createSecretKey() {
        try {
            // Base64로 인코딩된 키를 디코딩
            byte[] keyBytes = Base64.getDecoder().decode(secretKey);
            return new SecretKeySpec(keyBytes, ALGORITHM);
        } catch (IllegalArgumentException e) {
            // Base64가 아닌 경우 기존 방식 사용 (fallback)
            byte[] keyBytes = new byte[32];
            byte[] secretBytes = secretKey.getBytes(StandardCharsets.UTF_8);
            System.arraycopy(secretBytes, 0, keyBytes, 0, Math.min(secretBytes.length, 32));
            return new SecretKeySpec(keyBytes, ALGORITHM);
        }
    }

    /**
     * 랜덤 AES 키 생성 (초기화용)
     * @return Base64 인코딩된 32바이트 키
     */
    public static String generateRandomKey() {
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance(ALGORITHM);
            keyGen.init(256, new SecureRandom());
            SecretKey secretKey = keyGen.generateKey();
            return Base64.getEncoder().encodeToString(secretKey.getEncoded());
        } catch (Exception e) {
            throw new RuntimeException("키 생성 실패", e);
        }
    }
} 