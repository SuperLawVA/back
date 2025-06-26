package com.superlawva.domain.ml.dto;

import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@Data
@Schema(description = "계약서 직접 작성 및 특약 생성 요청 DTO")
public class ContractCreateRequest {
    @JsonProperty("userId")
    @Schema(description = "사용자 ID", required = true)
    @NotNull(message = "userId는 필수입니다.")
    private String userId;

    @JsonProperty("userQuery")
    @Schema(description = "특약 생성을 위한 사용자 쿼리", required = true)
    @NotEmpty(message = "userQuery는 1개 이상 입력해야 합니다.")
    private List<String> userQuery;

    @JsonProperty("articles")
    @Schema(description = "직접 입력한 계약 조항 목록 (선택)")
    private List<String> articles;
} 