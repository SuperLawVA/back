package com.superlawva.domain.document.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Field;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@org.springframework.data.mongodb.core.mapping.Document(collection = "documents")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Document {
    
    @Id
    private String id; // MongoDB는 ObjectId를 String으로 사용
    
    @Field("user_id")
    private Long userId;
    
    @Field("original_filename")
    private String originalFilename;
    
    @Field("encrypted_file_key")
    private String encryptedFileKey;
    
    @Field("document_type")
    private DocumentType documentType;
    
    @Field("mime_type")
    private String mimeType;
    
    @Field("file_size_bytes")
    private Long fileSizeBytes;
    
    @Field("status")
    private DocumentStatus status;
    
    @CreatedDate
    @Field("created_at")
    private LocalDateTime createdAt;
    
    // 파일 저장 방식 선택
    @Field("storage_type")
    @Builder.Default
    private StorageType storageType = StorageType.GRIDFS;
    
    // GridFS 파일 ID (대용량 파일용)
    @Field("gridfs_file_id")
    private String gridfsFileId;
    
    // 인라인 저장 (소용량 파일용)
    @Field("file_content")
    private byte[] fileContent;
    
    // 메타데이터
    @Field("metadata")
    private java.util.Map<String, Object> metadata;
    
    public enum DocumentType {
        LEASE_JEONSE, LEASE_MONTHLY, CERTIFICATE, CONTENT_PROOF, OTHER
    }
    
    public enum DocumentStatus {
        UPLOADED, OCR_PROCESSING, OCR_COMPLETED, OCR_FAILED
    }
    
    public enum StorageType {
        GRIDFS("GridFS 저장 (대용량)"),
        INLINE("인라인 저장 (소용량)"),
        EXTERNAL("외부 스토리지");
        
        private final String description;
        
        StorageType(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
} 