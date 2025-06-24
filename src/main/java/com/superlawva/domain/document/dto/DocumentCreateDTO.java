package com.superlawva.domain.document.dto;

import com.superlawva.domain.document.entity.Document;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentCreateDTO {
    
    @NotNull(message = "사용자 ID는 필수입니다")
    private Long userId;
    
    @NotBlank(message = "원본 파일명은 필수입니다")
    private String originalFilename;
    
    private Document.DocumentType documentType;
    
    private String mimeType;
    
    private Long fileSizeBytes;
} 