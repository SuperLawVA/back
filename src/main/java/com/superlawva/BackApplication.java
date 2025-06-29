package com.superlawva;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableScheduling
@EnableCaching
public class BackApplication {

    public static void main(String[] args) {
        System.out.println("🚀 SuperLawVA 백엔드 애플리케이션 시작...");

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
}
