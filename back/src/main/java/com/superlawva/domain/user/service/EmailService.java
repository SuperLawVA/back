    package com.superlawva.domain.user.service;

    import lombok.RequiredArgsConstructor;
    import lombok.extern.slf4j.Slf4j;
    import org.springframework.beans.factory.annotation.Value;
    import org.springframework.mail.javamail.JavaMailSender;
    import org.springframework.mail.javamail.MimeMessageHelper;
    import org.springframework.stereotype.Service;

    import jakarta.mail.internet.MimeMessage;


    @Slf4j
    @Service
    @RequiredArgsConstructor
    public class EmailService {

        // 🟢 메일 기능을 선택적으로 사용할 수 있도록 설정
        @Value("${app.email.enabled:false}")
        private boolean emailEnabled;

        @Value("${app.frontend.url:http://localhost:3000}")
        private String frontendUrl;

        // ConditionalOnProperty를 사용하여 메일 설정이 있을 때만 주입
        private final JavaMailSender mailSender;

        /**
         * 🟢 React 연동: 이메일 인증 코드 발송
         * - 실제 이메일 발송은 선택적
         * - 주로 로그 기록 및 React에서 처리할 데이터 반환
         */
        public EmailVerificationResult sendVerificationEmail(String toEmail, String verificationCode) {
            log.info("=== 이메일 인증 코드 발송 ===");
            log.info("대상 이메일: {}", toEmail);
            log.info("인증 코드: {}", verificationCode);
            log.info("프론트엔드 URL: {}/verify-email", frontendUrl);

            if (emailEnabled && mailSender != null) {
                try {
                    // 실제 이메일 발송
                    sendActualEmail(toEmail, verificationCode, "verification");
                    log.info("✅ 실제 이메일이 발송되었습니다: {}", toEmail);
                    return EmailVerificationResult.success("이메일이 발송되었습니다.", verificationCode);
                } catch (Exception e) {
                    log.error("❌ 이메일 발송 실패: {}", e.getMessage());
                    // 이메일 발송 실패해도 인증 코드는 반환 (개발 모드)
                    return EmailVerificationResult.devMode("이메일 발송 실패 (개발 모드)", verificationCode);
                }
            } else {
                // 개발 모드: 실제 이메일 발송 없이 코드만 로그 출력
                log.warn("🔧 개발 모드: 실제 이메일 발송 안함");
                log.warn("📧 인증 코드: {}", verificationCode);
                return EmailVerificationResult.devMode("개발 모드: 콘솔에서 인증 코드 확인", verificationCode);
            }
        }

        /**
         * 🟢 React 연동: 비밀번호 재설정 이메일 발송
         */
        public EmailVerificationResult sendPasswordResetEmail(String toEmail, String resetToken) {
            String resetUrl = frontendUrl + "/reset-password?token=" + resetToken;

            log.info("=== 비밀번호 재설정 이메일 발송 ===");
            log.info("대상 이메일: {}", toEmail);
            log.info("재설정 URL: {}", resetUrl);

            if (emailEnabled && mailSender != null) {
                try {
                    sendActualEmail(toEmail, resetToken, "reset");
                    log.info("✅ 비밀번호 재설정 이메일이 발송되었습니다: {}", toEmail);
                    return EmailVerificationResult.success("비밀번호 재설정 이메일이 발송되었습니다.", resetUrl);
                } catch (Exception e) {
                    log.error("❌ 이메일 발송 실패: {}", e.getMessage());
                    return EmailVerificationResult.devMode("이메일 발송 실패 (개발 모드)", resetUrl);
                }
            } else {
                log.warn("🔧 개발 모드: 실제 이메일 발송 안함");
                log.warn("🔗 재설정 URL: {}", resetUrl);
                return EmailVerificationResult.devMode("개발 모드: 콘솔에서 재설정 URL 확인", resetUrl);
            }
        }

        /**
         * 실제 이메일 발송 로직
         */
        private void sendActualEmail(String toEmail, String code, String type) throws Exception {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "utf-8");

            helper.setTo(toEmail);

            if ("verification".equals(type)) {
                helper.setSubject("AI 법률 서비스 - 이메일 인증");
                helper.setText(createSimpleVerificationTemplate(code), true);
            } else if ("reset".equals(type)) {
                helper.setSubject("AI 법률 서비스 - 비밀번호 재설정");
                helper.setText(createSimpleResetTemplate(code), true);
            }

            helper.setFrom("noreply@legal-ai.com", "AI 법률 서비스");
            mailSender.send(message);
        }

        /**
         * 🟢 간단한 인증 이메일 템플릿 (React 친화적)
         */
        private String createSimpleVerificationTemplate(String verificationCode) {
            return String.format("""
                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px;">
                    <h2 style="color: #007bff;">AI 법률 서비스</h2>
                    <h3>이메일 인증</h3>
                    <p>안녕하세요! 회원가입을 완료하기 위해 아래 인증 코드를 입력해주세요:</p>
                    <div style="background: #f8f9fa; padding: 20px; text-align: center; font-size: 24px; font-weight: bold; color: #007bff; border-radius: 5px; margin: 20px 0;">
                        %s
                    </div>
                    <p>또는 <a href="%s/verify-email?code=%s">여기를 클릭</a>하여 바로 인증하세요.</p>
                    <p><small>이 코드는 24시간 동안 유효합니다.</small></p>
                </div>
                """, verificationCode, frontendUrl, verificationCode);
        }

        /**
         * 🟢 간단한 비밀번호 재설정 템플릿
         */
        private String createSimpleResetTemplate(String resetToken) {
            String resetUrl = frontendUrl + "/reset-password?token=" + resetToken;
            return String.format("""
                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px;">
                    <h2 style="color: #dc3545;">AI 법률 서비스</h2>
                    <h3>비밀번호 재설정</h3>
                    <p>비밀번호 재설정을 요청하셨습니다.</p>
                    <p>아래 버튼을 클릭하여 새 비밀번호를 설정하세요:</p>
                    <div style="text-align: center; margin: 30px 0;">
                        <a href="%s" style="background: #dc3545; color: white; padding: 12px 24px; text-decoration: none; border-radius: 5px; display: inline-block;">비밀번호 재설정</a>
                    </div>
                    <p><small>이 링크는 1시간 동안 유효합니다.</small></p>
                </div>
                """, resetUrl);
        }

        /**
         * 🟢 이메일 발송 결과를 담는 클래스
         */
        public static class EmailVerificationResult {
            private final boolean success;
            private final String message;
            private final String data;  // 인증 코드 또는 URL
            private final boolean devMode;

            private EmailVerificationResult(boolean success, String message, String data, boolean devMode) {
                this.success = success;
                this.message = message;
                this.data = data;
                this.devMode = devMode;
            }

            public static EmailVerificationResult success(String message, String data) {
                return new EmailVerificationResult(true, message, data, false);
            }

            public static EmailVerificationResult devMode(String message, String data) {
                return new EmailVerificationResult(true, message, data, true);
            }

            public static EmailVerificationResult failure(String message) {
                return new EmailVerificationResult(false, message, null, false);
            }

            // Getters
            public boolean isSuccess() { return success; }
            public String getMessage() { return message; }
            public String getData() { return data; }
            public boolean isDevMode() { return devMode; }
        }
    }