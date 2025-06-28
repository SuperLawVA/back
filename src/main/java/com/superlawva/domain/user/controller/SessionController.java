package com.superlawva.domain.user.controller;

import com.superlawva.domain.user.dto.LoginResponseDTO;
import com.superlawva.domain.user.entity.User;
import com.superlawva.domain.user.service.UserService;
import com.superlawva.global.exception.BaseException;
import com.superlawva.global.response.ApiResponse;
import com.superlawva.global.response.status.ErrorStatus;
import com.superlawva.global.security.annotation.LoginUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "🔐 Authentication", description = "세션 관리 API")
public class SessionController {

    private final UserService userService;

    @GetMapping("/verify")
    @Operation(
        summary = "🔍 JWT 토큰 유효성 검증", 
        description = """
        JWT 토큰이 유효한지 간단하게 확인합니다.
        """
    )
    @SecurityRequirement(name = "JWT")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "✅ 토큰 유효",
            content = @Content(
                examples = @ExampleObject(
                    value = """
                    {
                        "isSuccess": true,
                        "code": "200",
                        "message": "요청에 성공했습니다.",
                        "result": true
                    }
                    """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401", 
            description = "❌ 토큰 무효 (JWT 필터에서 차단)",
            content = @Content(
                examples = @ExampleObject(
                    value = """
                    {
                        "timestamp": "2025-06-24T14:30:00.000+00:00",
                        "status": 401,
                        "error": "Unauthorized",
                        "path": "/auth/verify"
                    }
                    """
                )
            )
        )
    })
    public ApiResponse<Boolean> verifyToken(@Parameter(hidden = true) @LoginUser User user) {
        // JWT 필터를 통과했고 사용자가 존재하면 토큰 유효
        return ApiResponse.onSuccess(user != null);
    }

    @PostMapping("/logout")
    @Operation(
        summary = "🚪 로그아웃", 
        description = """
        현재 로그인한 사용자를 로그아웃 처리합니다.
        
        **동작 과정:**
        1. 액세스 토큰 유효성 검증
        2. 토큰에서 사용자 ID 추출
        3. Redis에서 해당 사용자의 리프레시 토큰 삭제
        4. 서버 측 세션 완전 차단
        
        **보안 특징:**
        - 액세스 토큰은 자연 만료 (보통 1-12시간)
        - 리프레시 토큰 즉시 삭제로 재발급 불가
        - 완전한 서버 측 로그아웃
        
        **사용법:**
        ```javascript
        const response = await fetch('/auth/logout', {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${localStorage.getItem('access_token')}`,
                'Content-Type': 'application/json'
            }
        });
        
        if (response.ok) {
            localStorage.removeItem('access_token');
            window.location.href = '/login';
        }
        ```
        
        **주의사항:**
        - 이 API는 LogoutFilter에서 처리됩니다 (이 컨트롤러 메서드는 실행되지 않음)
        - 성공 후 클라이언트에서 토큰 삭제 필수
        - LogoutFilter가 JWT 토큰 검증 후 Redis에서 리프레시 토큰 삭제
        """
    )
    @SecurityRequirement(name = "JWT")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "✅ 로그아웃 성공",
            content = @Content(
                examples = @ExampleObject(
                    value = """
                    {
                        "isSuccess": true,
                        "code": "200",
                        "message": "로그아웃이 성공적으로 처리되었습니다.",
                        "result": null
                    }
                    """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", 
            description = "❌ 토큰이 없음 (LogoutFilter에서 처리)",
            content = @Content(
                examples = @ExampleObject(
                    value = """
                    {
                        "isSuccess": false,
                        "code": "400",
                        "message": "토큰이 없습니다.",
                        "result": null
                    }
                    """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401", 
            description = "❌ 유효하지 않은 토큰 (LogoutFilter에서 처리)",
            content = @Content(
                examples = @ExampleObject(
                    value = """
                    {
                        "isSuccess": false,
                        "code": "401",
                        "message": "유효하지 않은 토큰입니다.",
                        "result": null
                    }
                    """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "500", 
            description = "❌ 서버 오류 (LogoutFilter에서 처리)",
            content = @Content(
                examples = @ExampleObject(
                    value = """
                    {
                        "isSuccess": false,
                        "code": "500",
                        "message": "로그아웃 처리 중 오류가 발생했습니다.",
                        "result": null
                    }
                    """
                )
            )
        )
    })
    public ApiResponse<String> logout() {
        // ⚠️ 주의: 이 메서드는 실제로 실행되지 않습니다!
        // 실제 로그아웃 처리는 LogoutFilter에서 /auth/logout POST 요청을 가로채서 처리합니다.
        // 이 메서드는 Swagger 문서화 목적으로만 존재합니다.
        return ApiResponse.onSuccess("로그아웃이 성공적으로 처리되었습니다.");
    }
} 