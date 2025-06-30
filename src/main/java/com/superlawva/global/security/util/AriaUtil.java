package com.superlawva.global.security.util;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.Security;
import java.util.Base64;

/**
 * ARIA 256-bit CBC 양방향 암호화 유틸리티.
 * (국내 공공기관 납품 시 요구되는 국산 알고리즘)
 * 
 * 스프링 빈으로 등록 후, @Value 로 주입된 키를 static 필드에 담아
 * AttributeConverter 등 Non-Spring 영역에서도 사용할 수 있게 한다.
 */
@Component
public class AriaUtil {

    private static final String TRANSFORMATION = "ARIA/CBC/PKCS7Padding";
    private static final String ALGORITHM = "ARIA";

    private static String secretKey; // Base64 또는 평문(최대 32byte) 키

    @Value("${aria.secret-key}")
    public void setSecretKey(String key) {
        AriaUtil.secretKey = key;
    }

    @PostConstruct
    public void registerProvider() {
        // 중복 등록 방지
        if (Security.getProvider("BC") == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    public static String encrypt(String plainText) {
        try {
            if (plainText == null) return null;

            SecretKeySpec secretKeySpec = createSecretKey();
            IvParameterSpec iv = new IvParameterSpec(secretKeySpec.getEncoded(), 0, 16); // CBC IV → 키 일부 사용 (권고 X, 데모용)

            Cipher cipher = Cipher.getInstance(TRANSFORMATION, "BC");
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, iv);
            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            throw new RuntimeException("ARIA 암호화 실패: " + e.getMessage(), e);
        }
    }

    public static String decrypt(String cipherText) {
        try {
            if (cipherText == null) return null;

            SecretKeySpec secretKeySpec = createSecretKey();
            IvParameterSpec iv = new IvParameterSpec(secretKeySpec.getEncoded(), 0, 16);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION, "BC");
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, iv);
            byte[] decoded = Base64.getDecoder().decode(cipherText);
            byte[] decrypted = cipher.doFinal(decoded);
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("ARIA 복호화 실패: " + e.getMessage(), e);
        }
    }

    private static SecretKeySpec createSecretKey() {
        // 비밀키 길이를 32byte (256-bit) 로 맞추되, 부족하면 0 패딩
        byte[] keyBytes = new byte[32];
        byte[] src = secretKey.getBytes(StandardCharsets.UTF_8);
        System.arraycopy(src, 0, keyBytes, 0, Math.min(src.length, keyBytes.length));
        return new SecretKeySpec(keyBytes, ALGORITHM);
    }

} 