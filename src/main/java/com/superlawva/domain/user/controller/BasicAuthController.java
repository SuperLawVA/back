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
@Tag(name = "🔐 Authentication", description = "기본 인증 API")
public class BasicAuthController {

    private final UserService userService;

    @PostMapping("/signup")
    @Operation(
        summary = "📝 일반 회원가입", 
        description = """
        ## 📖 API 설명
        이메일과 비밀번호를 사용하여 새로운 사용자를 등록합니다.
        
        
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
                        "code": "COMMON400",
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
                        "code": "USER409",
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
       
        **페이로드 (Payload) 내용:**
        ```json
        {
          "sub": "user@example.com",    // 이메일 (subject)
          "userId": 1,                  // 사용자 ID  
          "iat": 1703123456,           // 토큰 발급시간 (timestamp)
          "exp": 1703166656            // 토큰 만료시간 (timestamp)
        }
        ```
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
                                "nickname": "사용자",
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
            description = "❌ 인증 실패 (비밀번호 불일치)",
            content = @Content(
                examples = @ExampleObject(
                    value = """
                    {
                        "isSuccess": false,
                        "code": "USER401",
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
                        "code": "USER404",
                        "message": "존재하지 않는 사용자입니다.",
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