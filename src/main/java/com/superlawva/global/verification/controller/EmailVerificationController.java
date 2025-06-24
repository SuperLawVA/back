package com.superlawva.global.verification.controller;

import com.superlawva.global.response.ApiResponse;
import com.superlawva.global.verification.dto.request.EmailSendRequestDTO;
import com.superlawva.global.verification.dto.request.EmailVerifyRequestDTO;
import com.superlawva.global.verification.service.EmailVerificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/email")
@RequiredArgsConstructor
@Tag(name = "📧 Email Verification", description = "이메일 인증 코드 발송 및 검증")
public class EmailVerificationController {

    private final EmailVerificationService emailVerificationService;

    @PostMapping("/send")
    @Operation(
        summary = "📤 인증코드 이메일 발송", 
        description = """
        회원가입을 위한 이메일 인증 코드를 발송합니다.
        
        **사용법:**
        1. 가입하려는 이메일 주소를 입력
        2. 해당 이메일로 6자리 인증코드 발송
        3. 이메일 수신함에서 인증코드 확인
        4. `/api/email/verify` API로 인증코드 검증
        
        **요청 예시:**
        ```json
        {
            "email": "user@example.com"
        }
        ```
        
        **응답 예시:**
        ```json
        {
            "isSuccess": true,
            "code": "200",
            "message": "요청에 성공했습니다.",
            "result": null
        }
        ```
        
        **주의사항:**
        - 이미 가입된 이메일은 사용할 수 없습니다
        - 인증코드는 5분간 유효합니다
        - 동일 이메일로 재발송 시 기존 코드는 무효화됩니다
        """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "인증코드 발송 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "이미 가입된 이메일")
    })
    public ApiResponse<Void> sendVerificationEmail(@RequestBody @Valid EmailSendRequestDTO request) {
        emailVerificationService.sendVerificationEmail(request.getEmail());
        return ApiResponse.onSuccess(null);
    }

    @PostMapping("/verify")
    @Operation(
        summary = "✅ 인증코드 확인", 
        description = """
        발송된 인증코드를 확인하여 이메일 소유권을 검증합니다.
        
        **사용법:**
        1. 이메일 수신함에서 받은 6자리 인증코드 확인
        2. 이메일과 인증코드를 함께 전송
        3. 검증 성공 시 회원가입 진행 가능
        
        **요청 예시:**
        ```json
        {
            "email": "user@example.com",
            "code": "123456"
        }
        ```
        
        **응답 예시:**
        ```json
        {
            "isSuccess": true,
            "code": "200",
            "message": "요청에 성공했습니다.",
            "result": null
        }
        ```
        
        **에러 케이스:**
        - **400**: 인증코드가 일치하지 않거나 만료됨
        - **404**: 인증코드를 찾을 수 없음 (이메일 발송을 먼저 해야 함)
        
        **주의사항:**
        - 인증코드는 5분간만 유효합니다
        - 인증코드는 한 번만 사용 가능합니다
        - 검증 완료 후 즉시 회원가입을 진행하세요
        """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "이메일 인증 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "인증 코드가 일치하지 않거나 만료됨"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "인증 코드를 찾을 수 없음 (이메일 발송을 먼저 수행해야 함)")
    })
    public ApiResponse<Void> verifyEmail(@RequestBody @Valid EmailVerifyRequestDTO request) {
        emailVerificationService.verifyEmail(request);
        return ApiResponse.onSuccess(null);
    }
} 