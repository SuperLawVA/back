package com.superlawva.security;

import com.superlawva.global.security.util.AESUtil;
import com.superlawva.global.security.util.AriaUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * 간단한 유틸리티 레벨 양방향 암호화 테스트
 */
public class EncryptionUtilTests {

    private static final String PLAIN_TEXT = "SuperLawVA-양방향-암호화-테스트";
    private static final String KEY_32 = "0123456789ABCDEF0123456789ABCDEF"; // 32바이트 키

    @BeforeAll
    static void initKeys() {
        // AESUtil 의 secretKey 주입
        new AESUtil().setSecretKey(KEY_32);
        // AriaUtil 의 secretKey 주입 및 Provider 등록
        AriaUtil aria = new AriaUtil();
        aria.setSecretKey(KEY_32);
        aria.registerProvider();
    }

    @Test
    void aesEncryptDecrypt() {
        String enc = AESUtil.encrypt(PLAIN_TEXT);
        String dec = AESUtil.decrypt(enc);
        Assertions.assertEquals(PLAIN_TEXT, dec, "AES 복호화 결과가 원본과 일치해야 합니다.");
    }

    @Test
    void ariaEncryptDecrypt() {
        String enc = AriaUtil.encrypt(PLAIN_TEXT);
        String dec = AriaUtil.decrypt(enc);
        Assertions.assertEquals(PLAIN_TEXT, dec, "ARIA 복호화 결과가 원본과 일치해야 합니다.");
    }
} 