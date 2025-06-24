package com.superlawva.domain.ocr3.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.superlawva.domain.ocr3.entity.ContractData;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

// Error Response DTO
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorResponse {
    private String error;
    private String message;
    private String timestamp;
}