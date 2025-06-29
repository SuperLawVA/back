package com.superlawva.domain.ml.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Schema(description = "내용증명 생성 요청 DTO")
public class CertificateCreateRequest {

    @Schema(description = "내용증명의 기반이 될 계약서의 고유 ID", required = true, example = "contract-abc-123")
    @NotBlank(message = "계약서 ID는 필수입니다")
    private String contractId;

    @Schema(description = "내용증명을 요청하는 사용자의 고유 ID", required = true, example = "user-xyz-789")
    @NotBlank(message = "사용자 ID는 필수입니다")
    private String userId;

    @Schema(description = "내용증명에 포함될 핵심 주장 또는 요구사항", required = true, example = "보증금 5천만원을 계약 만료일인 2025년 10월 20일까지 반환해주시기 바랍니다.")
    @NotBlank(message = "사용자 요청사항은 필수입니다")
    private String userQuery;

    @Schema(description = "디버그 모드 활성화 여부", example = "false")
    private boolean debugMode = false;

    // Getter/Setter는 Lombok이 자동으로 생성하므로 명시적으로 작성할 필요가 없습니다.
}