package com.superlawva.domain.document.dto;

import com.superlawva.domain.document.entity.Document;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentResponseDTO {
    
    private String id; // MongoDB ObjectId
    private Long userId;
    private String originalFilename;
    private String encryptedFileKey;
    private Document.DocumentType documentType;
    private String mimeType;
    private Long fileSizeBytes;
    private Document.DocumentStatus status;
    private LocalDateTime createdAt;
    
    public static DocumentResponseDTO fromEntity(Document document) {
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