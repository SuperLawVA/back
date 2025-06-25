package com.superlawva.global.config;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

// 🚀 MongoDB Atlas 설정 (운영 전용)
@Slf4j
@Configuration
@EnableMongoRepositories(basePackages = {
        "com.superlawva.domain.ocr3.repository",
        "com.superlawva.domain.ml.repository"
})
public class MongoConfig extends AbstractMongoClientConfiguration {

    @Value("${spring.data.mongodb.uri}")
    private String mongoUri;

    @Override
    protected String getDatabaseName() {
        // MongoDB Atlas URI에서 데이터베이스 이름 동적 추출
        try {
            ConnectionString connectionString = new ConnectionString(mongoUri);
            String database = connectionString.getDatabase();
            String dbName = database != null ? database : "superlawva_docs";
            log.info("🔍 PROD MongoDB Atlas 설정 활성화 - Database: {}", dbName);
            return dbName;
        } catch (Exception e) {
            log.warn("MongoDB URI에서 데이터베이스 이름 추출 실패, 기본값 사용: superlawva_docs");
            return "superlawva_docs";
        }
    }

    @Override
    @Bean
    @Primary
    public MongoClient mongoClient() {
        log.info("🔍 PROD MongoDB Atlas 연결 시도...");
        log.info("🔍 MongoDB URI: {}", mongoUri.replaceAll("://([^:]+):([^@]+)@", "://***:***@"));

        try {
            ConnectionString connectionString = new ConnectionString(mongoUri);
            MongoClientSettings settings = MongoClientSettings.builder()
                    .applyConnectionString(connectionString)
                    .build();

            MongoClient client = MongoClients.create(settings);
            log.info("✅ MongoDB Atlas 클라이언트 생성 성공!");

            // 연결 테스트
            try {
                client.listDatabaseNames().first();
                log.info("✅ MongoDB Atlas 연결 테스트 성공!");
            } catch (Exception e) {
                log.error("❌ MongoDB Atlas 연결 테스트 실패: {}", e.getMessage());
            }

            return client;
        } catch (Exception e) {
            log.error("❌ MongoDB Atlas 클라이언트 생성 실패: {}", e.getMessage(), e);
            throw new RuntimeException("MongoDB Atlas 연결 실패: " + e.getMessage(), e);
        }
    }
} 