package com.superlawva.global.verification.service;

import com.superlawva.global.mail.MailService;
// import com.superlawva.global.security.util.AESUtil;
import com.superlawva.global.security.util.HashUtil;
import com.superlawva.global.exception.BaseException;
import com.superlawva.global.response.status.ErrorStatus;
import com.superlawva.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Random;

import com.superlawva.global.verification.dto.request.EmailVerifyRequestDTO;

@Service
@RequiredArgsConstructor
public class EmailVerificationService {

    private final MailService mailService;
    private final RedisTemplate<String, String> redisTemplate;
    // private final AESUtil aesUtil; // AES 임시 비활성화
    private final UserRepository userRepository;
    private final HashUtil hashUtil;

    @Value("${frontend.url:http://localhost:3000}")
    private String frontendUrl;

    public void sendVerification(String email) {
        String code = generate6DigitCode();
        String key = "email:verify:" + email;

        // 1. AES 암호화 임시 비활성화 - 평문으로 Redis에 저장 (30분 유효)
        // String encryptedCode = aesUtil.encrypt(code);
        redisTemplate.opsForValue().set(key, code, Duration.ofMinutes(30)); // 평문 저장

        // 2. HTML 형식 메일 본문 작성
        String subject = "이메일 인증번호";
        String body = makeMessageForm(code, "인증번호");

        try {
            mailService.sendEmail(email, subject, body);
        } catch (Exception e) {
            throw new RuntimeException("메일 전송 오류", e);
        }
    }

    public boolean verifyToken(String email, String code) {
        String key = "email:verify:" + email;
        String storedCode = redisTemplate.opsForValue().get(key); // 평문으로 가져오기

        if (storedCode != null) {
            try {
                // AES 복호화 비활성화 - 평문 비교
                // String storedCode = aesUtil.decrypt(encryptedStoredCode);
                if (storedCode.equals(code)) {
                    redisTemplate.delete(key);
                    return true;
                }
            } catch (Exception e) {
                return false; // 복호화 실패
            }
        }
        return false;
    }

    /**
     * 보안 인증 토큰 생성 (이메일 링크 인증용) - 임시 비활성화
     * @param email 사용자 이메일
     * @return 암호화된 토큰
     */
    public String generateSecureVerificationToken(String email) {
        // return aesUtil.generateSecureToken(email);
        return "temp_token_" + email + "_" + System.currentTimeMillis(); // 임시 토큰
    }

    /**
     * 보안 토큰 검증 - 임시 비활성화
     * @param token 검증할 토큰
     * @param email 예상되는 이메일
     * @return 토큰 유효성
     */
    public boolean verifySecureToken(String token, String email) {
        // AES 토큰 검증 임시 비활성화
        return token.contains(email); // 임시 검증 로직
        /*
        try {
            String extractedEmail = aesUtil.extractDataFromToken(token);
            boolean isValidTime = aesUtil.isTokenValid(token, Duration.ofHours(24).toMillis()); // 24시간 유효
            return email.equals(extractedEmail) && isValidTime;
        } catch (Exception e) {
            return false;
        }
        */
    }

    private String generate6DigitCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000); // 6자리 숫자
        return String.valueOf(code);
    }

    private String makeMessageForm(String value, String purpose) {
        return "<h1 style='text-align: center;'>[SuperLawVA]</h1>" +
                "<h3 style='text-align: center;'>" + purpose +
                " : <strong style='font-size: 32px; letter-spacing: 8px;'>" +
                value + "</strong></h3>";
    }

    public void sendVerificationEmail(String email) {
        // 이미 가입된 이메일인지 확인
        String emailHash = hashUtil.hash(email);
        if (userRepository.existsByEmailHash(emailHash)) {
            throw new BaseException(ErrorStatus._EMAIL_ALREADY_EXISTS);
        }
        sendVerification(email);
    }

    public void verifyEmail(EmailVerifyRequestDTO request) {
        String key = "email:verify:" + request.getEmail();
        String storedCode = redisTemplate.opsForValue().get(key); // 평문으로 가져오기

        if (storedCode == null) {
            throw new BaseException(ErrorStatus._VERIFICATION_CODE_NOT_FOUND);
        }

        try {
            // AES 복호화 비활성화 - 평문 비교
            // String storedCode = aesUtil.decrypt(encryptedStoredCode);
            if (!storedCode.equals(request.getCode())) {
                throw new BaseException(ErrorStatus._VERIFICATION_CODE_NOT_MATCH);
            }
            // 인증 성공 시 Redis에서 삭제
            redisTemplate.delete(key);
        } catch (Exception e) {
            throw new BaseException(ErrorStatus._VERIFICATION_CODE_NOT_MATCH);
        }
    }
}