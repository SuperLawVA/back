package com.superlawva.domain.user.controller;

import com.superlawva.domain.user.dto.LoginRequestDTO;
import com.superlawva.domain.user.dto.LoginResponseDTO;
import com.superlawva.domain.user.dto.UserRequestDTO;
import com.superlawva.domain.user.service.UserService;
import com.superlawva.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "🔐 Authentication · Basic", description = "기본 인증 (회원가입, 로그인)")
public class BasicAuthController {

    private final UserService userService;

    @PostMapping("/signup")
    @Operation(
        summary = "📝 일반 회원가입", 
        description = """
        이메일과 비밀번호를 사용하여 새로운 사용자를 등록합니다.
        
        **사용법:**
        1. 이메일, 비밀번호, 이름을 입력하여 요청
        2. 성공 시 회원가입 완료 메시지 반환
        3. 실패 시 적절한 에러 메시지 반환
        
        **주의사항:**
        - 이메일은 중복될 수 없습니다
        - 비밀번호는 암호화되어 저장됩니다
        - 비밀번호 복잡성 검증은 프론트엔드에서 처리됩니다
        - 회원가입 후 바로 로그인하려면 `/auth/login` API를 사용하세요
        """
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "✅ 회원가입 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class),
                examples = @ExampleObject(
                    value = """
                    {
                        "isSuccess": true,
                        "code": "200",
                        "message": "요청에 성공했습니다.",
                        "result": "회원가입이 성공적으로 완료되었습니다."
                    }
                    """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", 
            description = "❌ 잘못된 요청 데이터 (유효성 검사 실패)",
            content = @Content(
                examples = @ExampleObject(
                    value = """
                    {
                        "isSuccess": false,
                        "code": "400",
                        "message": "잘못된 요청입니다.",
                        "result": null
                    }
                    """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "409", 
            description = "❌ 이미 존재하는 이메일",
            content = @Content(
                examples = @ExampleObject(
                    value = """
                    {
                        "isSuccess": false,
                        "code": "4009",
                        "message": "이미 사용 중인 이메일입니다.",
                        "result": null
                    }
                    """
                )
            )
        )
    })
    public ApiResponse<String> signUp(@RequestBody @Valid UserRequestDTO request) {
        userService.register(request);
        return ApiResponse.onSuccess("회원가입이 성공적으로 완료되었습니다.");
    }

    @PostMapping("/login")
    @Operation(
        summary = "🔑 일반 로그인", 
        description = """
        이메일과 비밀번호로 로그인하고 JWT 토큰을 발급받습니다.
        
        **사용법:**
        1. 이메일과 비밀번호를 입력하여 요청
        2. 성공 시 JWT 토큰과 사용자 정보 반환
        3. 실패 시 적절한 에러 메시지 반환
        
        **응답 데이터:**
        - `token`: JWT 액세스 토큰 (Authorization 헤더에 "Bearer " + token 형태로 사용)
        - `user`: 사용자 상세 정보 (ID, 이메일, 이름, 알림, 계약, 채팅 등)
        
        **JWT 토큰 구조:**
        ```
        JWT = header.payload.signature
        예시: eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyQGV4YW1wbGUuY29tIiwidXNlcklkIjoxLCJpYXQiOjE3MDMxMjM0NTYsImV4cCI6MTcwMzE2NjY1Nn0.signature
        ```
        
        **페이로드 (Payload) 내용:**
        ```json
        {
          "sub": "user@example.com",    // 이메일 (subject)
          "userId": 1,                  // 사용자 ID  
          "iat": 1703123456,           // 토큰 발급시간 (timestamp)
          "exp": 1703166656            // 토큰 만료시간 (timestamp)
        }
        ```
        
        **토큰 사용법:**
        ```javascript
        // 1. 로그인 후 토큰 저장
        const loginResponse = await fetch('/auth/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email: 'user@example.com', password: 'password' })
        });
        const data = await loginResponse.json();
        localStorage.setItem('access_token', data.result.token);
        
        // 2. 이후 API 호출 시 토큰 사용
        const response = await fetch('/api/some-endpoint', {
            headers: {
                'Authorization': `Bearer ${localStorage.getItem('access_token')}`,
                'Content-Type': 'application/json'
            }
        });
        ```
        
        **토큰 특징:**
        - 유효기간: 24시간 (86,400,000ms)
        - 알고리즘: HS256 (HMAC SHA-256)
        - 서버에서 토큰 검증 시 페이로드의 userId를 사용하여 사용자 정보 조회
        - 토큰 만료 시 401 Unauthorized 응답
        """
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "✅ 로그인 성공",
            content = @Content(
                schema = @Schema(implementation = LoginResponseDTO.class),
                examples = @ExampleObject(
                    value = """
                    {
                        "isSuccess": true,
                        "code": "200",
                        "message": "요청에 성공했습니다.",
                        "result": {
                            "token": "eyJhbGciOiJIUzI1NiJ9...",
                            "user": {
                                "id": 1,
                                "email": "user@example.com",
                                "userName": "사용자",
                                "notification": [0, 1, 2],
                                "contractArray": [],
                                "recentChat": []
                            }
                        }
                    }
                    """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401", 
            description = "❌ 인증 실패 (이메일 또는 비밀번호 불일치)",
            content = @Content(
                examples = @ExampleObject(
                    value = """
                    {
                        "isSuccess": false,
                        "code": "USER400",
                        "message": "비밀번호가 일치하지 않습니다.",
                        "result": null
                    }
                    """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404", 
            description = "❌ 존재하지 않는 사용자",
            content = @Content(
                examples = @ExampleObject(
                    value = """
                    {
                        "isSuccess": false,
                        "code": "MEMBER4001",
                        "message": "사용자가 없습니다.",
                        "result": null
                    }
                    """
                )
            )
        )
    })
    public ApiResponse<LoginResponseDTO> login(@RequestBody @Valid LoginRequestDTO request) {
        return ApiResponse.onSuccess(userService.login(request));
    }
} 