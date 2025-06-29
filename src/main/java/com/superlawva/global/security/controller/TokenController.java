package com.superlawva.global.security.controller;

import com.superlawva.domain.user.entity.User;
import com.superlawva.domain.user.repository.UserRepository;
import com.superlawva.global.exception.BaseException;
import com.superlawva.global.response.ApiResponse;
import com.superlawva.global.response.status.ErrorStatus;
import com.superlawva.global.security.util.HashUtil;
import com.superlawva.global.security.util.JwtTokenProvider;
import com.superlawva.global.security.annotation.LoginUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Tag(name = "⚠️ Test & Tools", description = "개발 및 테스트용 API")
@RequiredArgsConstructor
@RestController
public class TokenController {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final HashUtil hashUtil;

    @PostMapping("/regenerate")
    @Operation(
        summary = "🔄 액세스 토큰 재발급",
        description = """
        현재 유효한 JWT 토큰을 사용하여 새로운 액세스 토큰을 발급받습니다.
        
        **사용 목적:**
        - 토큰 만료 전 미리 갱신
        - 보안 강화를 위한 정기적 토큰 교체
        
        **사용법:**
        ```javascript
        const response = await fetch('/regenerate', {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${localStorage.getItem('access_token')}`,
                'Content-Type': 'application/json'
            }
        });
        
        const data = await response.json();
        if (data.isSuccess) {
            localStorage.setItem('access_token', data.result.accessToken);
        }
        ```
        """
    )
    @SecurityRequirement(name = "JWT")
    public ApiResponse<Map<String, String>> regenerateToken(@Parameter(hidden = true) @LoginUser User user) {
        // 인증된 사용자가 없는 경우 에러
        if (user == null) {
            throw new BaseException(ErrorStatus.UNAUTHORIZED);
        }

        // 새로운 액세스 토큰 생성
        String newAccessToken = jwtTokenProvider.createToken(user.getEmail(), user.getId());
        
        // 새로운 액세스 토큰 반환
        return ApiResponse.onSuccess(Map.of("accessToken", newAccessToken));
    }
}
