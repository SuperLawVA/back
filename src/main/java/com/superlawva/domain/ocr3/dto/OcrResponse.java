package com.superlawva.domain.ocr3.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.superlawva.domain.ocr3.entity.ContractData;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

// Final Response DTO
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OcrResponse {
    @JsonProperty("contract_data")
    private ContractData contractData;
    
    @JsonProperty("debug_mode")
    private boolean debugMode;
}