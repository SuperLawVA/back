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
    @Schema(description = "사용자를 식별하는 고유 ID", required = true, example = "user-12345")
    @NotNull(message = "userId는 필수입니다.")
    private String userId;

    @JsonProperty("userQuery")
    @Schema(
        description = "AI 특약 생성을 위한 사용자의 요구사항. 자유로운 형식의 문장으로 요청합니다.", 
        required = true,
        example = "[\"전세 사기가 걱정되니, 보증금을 안전하게 지킬 수 있는 조항을 추가해주세요.\", \"집주인이 바뀌더라도 임대차 계약이 유지되도록 하는 내용도 포함해주세요.\"]"
    )
    @NotEmpty(message = "userQuery는 1개 이상 입력해야 합니다.")
    private List<String> userQuery;

    @JsonProperty("articles")
    @Schema(
        description = "사용자가 직접 입력하는 계약서의 기본 조항 목록입니다.", 
        example = "[\"제1조 (목적) 본 계약은 ...\", \"제2조 (보증금) 임차인은 보증금 1억 원을 ...\"]"
    )
    private List<String> articles;
}