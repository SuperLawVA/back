package com.superlawva.domain.document.dto;

import com.superlawva.domain.document.entity.DocumentEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "문서 응답 DTO")
public class DocumentResponseDTO {
    
    @Schema(description = "문서 ID", example = "123")
    private Long id; // JPA Long ID
    
    @Schema(description = "사용자 ID", example = "1")
    private Long userId;
    
    @Schema(description = "원본 파일명", example = "contract.pdf")
    private String originalFilename;
    
    @Schema(description = "암호화된 파일 키", example = "encrypted_key_abc123")
    private String encryptedFileKey;
    
    @Schema(description = "문서 타입", example = "CONTRACT")
    private DocumentEntity.DocumentType documentType;
    
    @Schema(description = "MIME 타입", example = "application/pdf")
    private String mimeType;
    
    @Schema(description = "파일 크기 (바이트)", example = "1024000")
    private Long fileSizeBytes;
    
    @Schema(description = "문서 상태", example = "PROCESSING")
    private DocumentEntity.DocumentStatus status;
    
    @Schema(description = "생성일시", example = "2025-01-15T10:30:00")
    private LocalDateTime createdAt;
    
    public static DocumentResponseDTO fromEntity(DocumentEntity document) {
        return DocumentResponseDTO.builder()
            .id(document.getId())
            .userId(document.getUserId())
            .originalFilename(document.getOriginalFilename())
            .encryptedFileKey(document.getEncryptedFileKey())
            .documentType(document.getDocumentType())
            .mimeType(document.getMimeType())
            .fileSizeBytes(document.getFileSizeBytes())
            .status(document.getStatus())
            .createdAt(document.getCreatedAt())
            .build();
    }
} 