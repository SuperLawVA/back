package com.superlawva.domain.ml.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class CertificateCreateRequest {

    @NotBlank(message = "계약서 ID는 필수입니다")
    private String contractId;

    @NotBlank(message = "사용자 ID는 필수입니다")
    private String userId;

    @NotBlank(message = "사용자 요청사항은 필수입니다")
    private String userQuery;

    private boolean debugMode = false; // analysis와 동일한 구조

    // Getter/Setter 메서드들
    public String getContractId() { return contractId; }
    public void setContractId(String contractId) { this.contractId = contractId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUserQuery() { return userQuery; }
    public void setUserQuery(String userQuery) { this.userQuery = userQuery; }

    public boolean isDebugMode() { return debugMode; }
    public void setDebugMode(boolean debugMode) { this.debugMode = debugMode; }
}