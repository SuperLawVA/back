package com.superlawva.global.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.superlawva.global.response.status.SuccessStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonPropertyOrder({"isSuccess", "code", "message", "result"})
@Schema(
    description = "API 표준 응답",
    example = """
    {
      \"isSuccess\": true,
      \"code\": \"200\",
      \"message\": \"요청에 성공했습니다.\",
      \"result\": {
        \"id\": 123,
        \"name\": \"홍길동\"
      }
    }
    """
)
public class ApiResponse<T> {

    @Schema(description = "성공 여부", example = "true")
    @JsonProperty("isSuccess")
    private Boolean isSuccess;
    @Schema(description = "응답 코드", example = "200")
    private String code;
    @Schema(description = "메시지", example = "요청에 성공했습니다.")
    private String message;
    @Schema(description = "실제 데이터")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private T result;

    public static <T> ApiResponse<T> onSuccess(T data) {
        return new ApiResponse<>(true, SuccessStatus._OK.getCode(), SuccessStatus._OK.getMessage(), data);
    }

    public static <T> ApiResponse<T> of(SuccessStatus status, T data) {
        return new ApiResponse<>(true, status.getCode(), status.getMessage(), data);
    }

    public static ApiResponse<Object> onFailure(String code, String message, Object data) {
        return new ApiResponse<>(false, code, message, data);
    }

    public static <T> ApiResponse<T> success(T result) {
        return new ApiResponse<>(true, "200", "요청에 성공했습니다.", result);
    }

    public static <T> ApiResponse<T> error(String code, String message) {
        return new ApiResponse<>(false, code, message, null);
    }

    public boolean getIsSuccess() { return isSuccess; }
    public String getCode() { return code; }
    public String getMessage() { return message; }
    public T getResult() { return result; }
} 