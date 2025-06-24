package com.superlawva.domain.ocr3.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.superlawva.domain.ocr3.entity.ContractData;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

// Request DTO for OCR processing
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OcrRequest {
    private MultipartFile file;
}