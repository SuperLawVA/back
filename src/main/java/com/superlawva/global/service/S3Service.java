package com.superlawva.global.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3Service {

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @Value("${aws.s3.access-key}")
    private String accessKey;

    @Value("${aws.s3.secret-key}")
    private String secretKey;

    @Value("${aws.s3.region}")
    private String region;

    private S3Client s3Client;

    /**
     * S3 클라이언트 초기화
     */
    private void initializeS3Client() {
        if (s3Client == null) {
            try {
                s3Client = S3Client.builder()
                        .region(Region.of(region))
                        .credentialsProvider(StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(accessKey, secretKey)))
                        .build();
                log.info("S3 클라이언트 초기화 성공 - Region: {}", region);
            } catch (Exception e) {
                log.error("S3 클라이언트 초기화 실패: {}", e.getMessage(), e);
                throw new RuntimeException("S3 클라이언트 초기화 실패", e);
            }
        }
    }

    /**
     * 이미지 파일을 S3에 업로드
     */
    public String uploadImage(MultipartFile file, String userId) throws IOException {
        log.info("이미지 업로드 시작 - 파일명: {}, 사용자: {}", file.getOriginalFilename(), userId);
        
        initializeS3Client();
        
        try {
            // 파일명 생성 (중복 방지)
            String originalFilename = file.getOriginalFilename();
            String fileExtension = getFileExtension(originalFilename);
            String fileName = generateFileName(userId, fileExtension);
            
            // S3 키 생성
            String s3Key = generateS3Key(userId, fileName);
            
            // S3에 업로드
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .contentType(file.getContentType())
                    .contentLength(file.getSize())
                    .build();

            PutObjectResponse response = s3Client.putObject(request, 
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
            
            // S3 URL 생성
            String s3Url = generateS3Url(s3Key);
            
            log.info("이미지 업로드 완료 - S3 URL: {}", s3Url);
            return s3Url;
            
        } catch (Exception e) {
            log.error("이미지 업로드 실패: {}", e.getMessage(), e);
            throw new RuntimeException("이미지 업로드 실패: " + e.getMessage(), e);
        }
    }

    /**
     * 파일 확장자 추출
     */
    private String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf(".") == -1) {
            return "";
        }
        return filename.substring(filename.lastIndexOf("."));
    }

    /**
     * 고유한 파일명 생성
     */
    private String generateFileName(String userId, String fileExtension) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        return String.format("%s_%s_%s%s", userId, timestamp, uuid, fileExtension);
    }

    /**
     * S3 키 생성
     */
    private String generateS3Key(String userId, String fileName) {
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        return String.format("images/%s/%s/%s", date, userId, fileName);
    }

    /**
     * S3 URL 생성
     */
    private String generateS3Url(String s3Key) {
        return String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, region, s3Key);
    }

    /**
     * S3에서 파일 삭제
     */
    public void deleteImage(String s3Url) {
        log.info("이미지 삭제 시작 - S3 URL: {}", s3Url);
        
        try {
            initializeS3Client();
            
            // S3 URL에서 키 추출
            String s3Key = extractS3KeyFromUrl(s3Url);
            
            s3Client.deleteObject(builder -> builder
                    .bucket(bucketName)
                    .key(s3Key)
                    .build());
            
            log.info("이미지 삭제 완료 - S3 Key: {}", s3Key);
            
        } catch (Exception e) {
            log.error("이미지 삭제 실패: {}", e.getMessage(), e);
            throw new RuntimeException("이미지 삭제 실패: " + e.getMessage(), e);
        }
    }

    /**
     * S3 URL에서 키 추출
     */
    public String extractS3KeyFromUrl(String s3Url) {
        String prefix = String.format("https://%s.s3.%s.amazonaws.com/", bucketName, region);
        if (s3Url.startsWith(prefix)) {
            return s3Url.substring(prefix.length());
        }
        throw new IllegalArgumentException("유효하지 않은 S3 URL: " + s3Url);
    }

    public String uploadImage(MultipartFile file, String userId, String baseDir) throws IOException {
        log.info("이미지 업로드 시작(커스텀 경로) - 파일명: {}, 사용자: {}, baseDir: {}", file.getOriginalFilename(), userId, baseDir);
        initializeS3Client();
        try {
            String originalFilename = file.getOriginalFilename();
            String fileExtension = getFileExtension(originalFilename);
            String fileName = generateFileName(userId, fileExtension);
            String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            // key : {baseDir}/{date}/{userId}/{fileName}
            String s3Key = String.format("%s/%s/%s/%s", baseDir, date, userId, fileName);

            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .contentType(file.getContentType())
                    .contentLength(file.getSize())
                    .build();
            s3Client.putObject(request, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
            return generateS3Url(s3Key);
        } catch (Exception e) {
            log.error("이미지 업로드 실패(커스텀 경로): {}", e.getMessage(), e);
            throw new RuntimeException("이미지 업로드 실패: " + e.getMessage(), e);
        }
    }

    public byte[] downloadBytes(String s3Key) {
        initializeS3Client();
        try {
            return s3Client.getObject(builder -> builder.bucket(bucketName).key(s3Key).build()).readAllBytes();
        } catch (software.amazon.awssdk.services.s3.model.S3Exception e) {
            if (e.statusCode() == 403) {
                log.error("S3 접근 권한 오류(403): S3 Key = {}", s3Key);
            }
            log.error("S3 다운로드 실패: {}", e.getMessage(), e);
            throw new RuntimeException("S3 다운로드 실패: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("S3 다운로드 실패: {}", e.getMessage(), e);
            throw new RuntimeException("S3 다운로드 실패: " + e.getMessage(), e);
        }
    }

    public String uploadBytes(byte[] data, String userId, String baseDir, String fileExtension, String contentType) {
        initializeS3Client();
        try {
            String fileName = generateFileName(userId, fileExtension);
            String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            String s3Key = String.format("%s/%s/%s/%s", baseDir, date, userId, fileName);

            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .contentType(contentType)
                    .contentLength((long) data.length)
                    .build();
            s3Client.putObject(request, RequestBody.fromBytes(data));
            return generateS3Url(s3Key);
        } catch (Exception e) {
            log.error("업로드 실패(uploadBytes): {}", e.getMessage(), e);
            throw new RuntimeException("S3 업로드 실패: " + e.getMessage(), e);
        }
    }
} 