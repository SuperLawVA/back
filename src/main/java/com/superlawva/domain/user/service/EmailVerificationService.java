//package com.superlawva.domain.user.service;
//
//import jakarta.mail.MessagingException;
//import jakarta.mail.internet.MimeMessage;
//import lombok.RequiredArgsConstructor;
//import org.springframework.data.redis.core.StringRedisTemplate;
//import org.springframework.mail.javamail.JavaMailSender;
//import org.springframework.mail.javamail.MimeMessageHelper;
//import org.springframework.stereotype.Service;
//
//import java.time.Duration;
//import java.util.Random;
//
//@Service
//@RequiredArgsConstructor
//public class EmailVerificationService {
//
//    private final JavaMailSender mailSender;
//    private final StringRedisTemplate redisTemplate;
//    private static final String PREFIX = "verify:";
//
//    public void sendVerification(String email) {
//        String code = generateCode();
//        redisTemplate.opsForValue().set(PREFIX + email, code, Duration.ofMinutes(3));
//        sendMail(email, code);
//    }
//
//    public boolean verifyToken(String email, String inputCode) {
//        String saved = redisTemplate.opsForValue().get(PREFIX + email);
//        return inputCode != null && inputCode.equals(saved);
//    }
//
//    private void sendMail(String to, String code) {
//        try {
//            MimeMessage message = mailSender.createMimeMessage();
//            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
//            helper.setTo(to);
//            helper.setSubject("[Superlawva] 이메일 인증 코드입니다.");
//            helper.setText("<p>인증 코드: <strong>" + code + "</strong></p>", true); // HTML
//            mailSender.send(message);
//        } catch (MessagingException e) {
//            throw new RuntimeException("메일 전송 실패", e);
//        }
//    }
//
//    private String generateCode() {
//        Random r = new Random();
//        return String.format("%06d", r.nextInt(1000000));
//    }
//}