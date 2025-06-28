package com.superlawva.global.log;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import lombok.extern.slf4j.Slf4j;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class S3LogAppender extends AppenderBase<ILoggingEvent> {

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @Value("${aws.s3.access-key}")
    private String accessKey;

    @Value("${aws.s3.secret-key}")
    private String secretKey;

    @Value("${aws.s3.region}")
    private String region;

    private S3Client s3Client;
    private BlockingQueue<String> logBuffer;
    private ScheduledExecutorService scheduler;
    private ScheduledFuture<?> uploadTask;
    private static final int BUFFER_SIZE = 100;
    private static final int UPLOAD_INTERVAL_SECONDS = 60;

    @Override
    public void start() {
        if (!isStarted()) {
            // 환경변수 읽기
            loadEnvironmentVariables();
            
            // 필수 환경변수 검증
            if (isValidConfiguration()) {
            initializeS3Client();
            initializeBuffer();
            startUploadScheduler();
            super.start();
                addInfo("S3LogAppender started successfully. Bucket: " + bucketName);
            } else {
                addWarn("S3LogAppender 시작 실패: AWS 환경변수가 설정되지 않았습니다.");
            }
        }
    }

    private void loadEnvironmentVariables() {
        bucketName = getEnvOrDefault("AWS_S3_BUCKET_NAME", "superlawva-logs");
        region = getEnvOrDefault("AWS_REGION", "ap-northeast-2");
        accessKey = System.getenv("AWS_ACCESS_KEY_ID");
        secretKey = System.getenv("AWS_SECRET_ACCESS_KEY");
        
        addInfo("S3LogAppender 설정 로드됨 - Bucket: " + bucketName + ", Region: " + region);
    }

    private String getEnvOrDefault(String key, String defaultValue) {
        String value = System.getenv(key);
        return value != null ? value : defaultValue;
    }

    private boolean isValidConfiguration() {
        if (accessKey == null || accessKey.trim().isEmpty()) {
            addWarn("AWS_ACCESS_KEY_ID 환경변수가 설정되지 않았습니다.");
            return false;
        }
        if (secretKey == null || secretKey.trim().isEmpty()) {
            addWarn("AWS_SECRET_ACCESS_KEY 환경변수가 설정되지 않았습니다.");
            return false;
        }
        return true;
    }

    @Override
    public void stop() {
        if (isStarted()) {
            uploadRemainingLogs();
            if (uploadTask != null) {
                uploadTask.cancel(false);
            }
            if (scheduler != null) {
                scheduler.shutdown();
            }
            if (s3Client != null) {
                s3Client.close();
            }
            super.stop();
        }
    }

    private void initializeS3Client() {
        try {
            s3Client = S3Client.builder()
                    .region(Region.of(region))
                    .credentialsProvider(StaticCredentialsProvider.create(
                            AwsBasicCredentials.create(accessKey, secretKey)))
                    .build();
            addInfo("S3 클라이언트 초기화 성공 - Region: " + region);
        } catch (Exception e) {
            addError("S3 클라이언트 초기화 실패", e);
        }
    }

    private void initializeBuffer() {
        logBuffer = new LinkedBlockingQueue<>(BUFFER_SIZE);
    }

    private void startUploadScheduler() {
        scheduler = java.util.concurrent.Executors.newSingleThreadScheduledExecutor();
        uploadTask = scheduler.scheduleAtFixedRate(
                this::uploadBufferedLogs,
                UPLOAD_INTERVAL_SECONDS,
                UPLOAD_INTERVAL_SECONDS,
                TimeUnit.SECONDS
        );
    }

    @Override
    protected void append(ILoggingEvent event) {
        if (!isStarted()) {
            return;
        }

        try {
            String logEntry = formatLogEntry(event);
            if (!logBuffer.offer(logEntry)) {
                // 버퍼가 가득 찬 경우 즉시 업로드
                uploadBufferedLogs();
                logBuffer.offer(logEntry);
            }
        } catch (Exception e) {
            addError("Failed to append log event", e);
        }
    }

    private String formatLogEntry(ILoggingEvent event) {
        return String.format("%s [%s] %s %s - %s%n",
                event.getTimeStamp(),
                event.getThreadName(),
                event.getLevel(),
                event.getLoggerName(),
                event.getFormattedMessage()
        );
    }

    private void uploadBufferedLogs() {
        if (logBuffer.isEmpty()) {
            return;
        }

        try {
            StringBuilder logs = new StringBuilder();
            String logEntry;
            int count = 0;

            while ((logEntry = logBuffer.poll()) != null && count < BUFFER_SIZE) {
                logs.append(logEntry);
                count++;
            }

            if (logs.length() > 0) {
                uploadToS3(logs.toString());
            }
        } catch (Exception e) {
            addError("Failed to upload buffered logs", e);
        }
    }

    private void uploadToS3(String logs) {
        try {
            String key = generateS3Key();
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType("text/plain")
                    .build();

            s3Client.putObject(request, RequestBody.fromInputStream(
                    new ByteArrayInputStream(logs.getBytes(StandardCharsets.UTF_8)),
                    logs.length()
            ));
            
            addInfo("Logs uploaded to S3: " + key);
        } catch (Exception e) {
            addError("Failed to upload logs to S3", e);
        }
    }

    private String generateS3Key() {
        LocalDateTime now = LocalDateTime.now();
        String date = now.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String timestamp = now.format(DateTimeFormatter.ofPattern("HH-mm-ss"));
        return String.format("logs/%s/%s.log", date, timestamp);
    }

    private void uploadRemainingLogs() {
        uploadBufferedLogs();
    }
} 