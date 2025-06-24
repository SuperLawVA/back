package com.superlawva.global.config;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

// 🟢 로컬 프로필용 MongoDB 설정
@Configuration
@EnableMongoRepositories(basePackages = {
    "com.superlawva.domain.document.repository",
    "com.superlawva.domain.ocr3.repository"
})
@Profile("local")
public class MongoConfig extends AbstractMongoClientConfiguration {

    @Value("${spring.data.mongodb.host:localhost}")
    private String host;

    @Value("${spring.data.mongodb.port:27017}")
    private int port;

    @Value("${spring.data.mongodb.database:superlawva_docs}")
    private String database;

    @Value("${spring.data.mongodb.username:admin}")
    private String username;

    @Value("${spring.data.mongodb.password:password}")
    private String password;

    @Value("${spring.data.mongodb.authentication-database:admin}")
    private String authDatabase;

    @Override
    protected String getDatabaseName() {
        return database;
    }

    @Override
    @Bean
    public MongoClient mongoClient() {
        String connectionString = String.format(
            "mongodb://%s:%s@%s:%d/%s?authSource=%s",
            username, password, host, port, database, authDatabase
        );
        
        ConnectionString connString = new ConnectionString(connectionString);
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(connString)
                .build();
        
        return MongoClients.create(settings);
    }
}

// 🚀 운영 프로필용 MongoDB Atlas 설정
@Configuration
@EnableMongoRepositories(basePackages = {
    "com.superlawva.domain.document.repository",
    "com.superlawva.domain.ocr3.repository"
})
@Profile("prod")
class MongoAtlasConfig extends AbstractMongoClientConfiguration {

    @Value("${spring.data.mongodb.uri}")
    private String mongoUri;

    @Override
    protected String getDatabaseName() {
        // MongoDB Atlas URI에서 데이터베이스 이름 추출
        return "superlawva_docs";
    }

    @Override
    @Bean
    public MongoClient mongoClient() {
        ConnectionString connectionString = new ConnectionString(mongoUri);
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(connectionString)
                .build();
        
        return MongoClients.create(settings);
    }
} 