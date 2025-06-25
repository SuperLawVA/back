package com.superlawva;

import com.superlawva.domain.user.entity.User;
import com.superlawva.domain.user.repository.UserRepository;
import io.github.cdimascio.dotenv.Dotenv;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BackApplication {

    public static void main(String[] args) {
        System.out.println("🚀 SuperLawVA 백엔드 애플리케이션 시작...");

        // Docker/컨테이너 환경에서는 Docker Compose가 .env 파일을 환경변수로 주입해주므로,
        // Dotenv 라이브러리를 통한 수동 로드는 불필요하며 문제를 일으킬 수 있어 제거합니다.
        // Spring Boot가 자동으로 환경변수를 인식합니다.

        // 중요한 환경변수 확인 (System.getenv로 통일)
        System.out.println("📋 환경변수 확인:");
        System.out.println("  - SPRING_PROFILES_ACTIVE: " + System.getenv("SPRING_PROFILES_ACTIVE"));
        System.out.println("  - DATABASE_URL: " + (System.getenv("DATABASE_URL") != null ? "설정됨" : "없음"));
        System.out.println("  - MONGODB_URI: " + (System.getenv("MONGODB_URI") != null ? "설정됨" : "없음"));
        System.out.println("  - JWT_SECRET: " + (System.getenv("JWT_SECRET") != null ? "설정됨" : "없음"));

        try {
            System.out.println("🌱 Spring Boot 애플리케이션 시작 중...");
            SpringApplication.run(BackApplication.class, args);
            System.out.println("✅ Spring Boot 애플리케이션 시작 완료!");
        } catch (Exception e) {
            System.err.println("❌ Spring Boot 애플리케이션 시작 실패: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @Bean
    @ConditionalOnProperty(name = "app.create-test-users", havingValue = "true", matchIfMissing = true)
    public CommandLineRunner createTestUsers(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            try {
                System.out.println("🚀 테스트 사용자 생성 시작...");
                
                // 테스트용 사용자가 없으면 생성 (평문 비밀번호)
                if (!userRepository.existsByEmail("test@example.com")) {
                    User testUser = User.builder()
                            .email("test@example.com")
                            .password("password123")  // 평문으로 저장
                            .nickname("테스트사용자")
                            .provider("LOCAL")
                            .role(User.Role.USER)
                            .emailVerified(true)
                            .build();
                    userRepository.save(testUser);
                    System.out.println("✅ 테스트 사용자 생성 완료: test@example.com / password123");
                } else {
                    System.out.println("ℹ️ 테스트 사용자 이미 존재: test@example.com");
                }

                if (!userRepository.existsByEmail("admin@example.com")) {
                    User adminUser = User.builder()
                            .email("admin@example.com")
                            .password("admin123")  // 평문으로 저장
                            .nickname("관리자")
                            .provider("LOCAL")
                            .role(User.Role.ADMIN)
                            .emailVerified(true)
                            .build();
                    userRepository.save(adminUser);
                    System.out.println("✅ 관리자 사용자 생성 완료: admin@example.com / admin123");
                } else {
                    System.out.println("ℹ️ 관리자 사용자 이미 존재: admin@example.com");
                }

                if (!userRepository.existsByEmail("demo@example.com")) {
                    User demoUser = User.builder()
                            .email("demo@example.com")
                            .password("demo123")  // 평문으로 저장
                            .nickname("데모사용자")
                            .provider("LOCAL")
                            .role(User.Role.USER)
                            .emailVerified(true)
                            .build();
                    userRepository.save(demoUser);
                    System.out.println("✅ 데모 사용자 생성 완료: demo@example.com / demo123");
                } else {
                    System.out.println("ℹ️ 데모 사용자 이미 존재: demo@example.com");
                }
                
                System.out.println("🎉 테스트 사용자 생성 과정 완료");
            } catch (Exception e) {
                System.err.println("⚠️ 테스트 사용자 생성 중 오류 발생: " + e.getMessage());
                e.printStackTrace();
                // 에러가 발생해도 애플리케이션은 정상 시작되도록 함
            }
        };
    }
}
