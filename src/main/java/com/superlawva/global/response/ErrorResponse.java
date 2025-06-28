package com.superlawva.global.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "API 에러 응답")
public class ErrorResponse {
    @Schema(description = "에러 코드", example = "COMMON404")
    private String code;
    @Schema(description = "에러 메시지", example = "리소스를 찾을 수 없습니다.")
    private String message;

    public ErrorResponse() {}
    public ErrorResponse(String code, String message) {
        this.code = code;
        this.message = message;
    }
    public String getCode() { return code; }
    public String getMessage() { return message; }
} 