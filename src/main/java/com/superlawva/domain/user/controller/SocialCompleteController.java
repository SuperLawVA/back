package com.superlawva.domain.user.controller;

import com.superlawva.domain.user.dto.LoginResponseDTO;
import com.superlawva.domain.user.dto.SocialLoginCompleteDTO;
import com.superlawva.domain.user.entity.User;
import com.superlawva.domain.user.repository.UserRepository;
import com.superlawva.global.response.ApiResponse;
import com.superlawva.global.security.util.HashUtil;
import com.superlawva.global.security.util.JwtTokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.jsonwebtoken.Claims;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/auth/social")
@RequiredArgsConstructor
@Tag(name = "🔐 Social Authentication", description = "소셜 로그인 완료 API")
public class SocialCompleteController {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final HashUtil hashUtil;

    @PostMapping("/complete")
    @Operation(
        summary = "✅ 소셜 로그인 완료 (이메일 입력)", 
        description = """
     
        """
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "✅ 소셜 로그인 완료 성공",
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
                                "userName": "카카오사용자",
                                "notification": [],
                                "contractArray": [],
                                "recentChat": []
                            }
                        }
                    }
                    """
                )
            )
        )
    })
    public ApiResponse<LoginResponseDTO> completeSocialLogin(@RequestBody @Valid SocialLoginCompleteDTO request) {
        try {
            // 1. 임시 토큰에서 소셜 정보 추출
            Claims tempClaims = jwtTokenProvider.getTempTokenClaims(request.getTempToken());
            String socialId = tempClaims.get("socialId", String.class);
            String provider = tempClaims.get("provider", String.class);
            String nickname = tempClaims.get("nickname", String.class);
            
            // 2. 사용자 등록 또는 조회
            User user = userRepository.findByEmail(request.getEmail())
                    .orElseGet(() -> {
                        User.UserBuilder userBuilder = User.builder()
                                .email(request.getEmail())
                                .nickname(nickname)
                                .provider(provider)
                                .role(User.Role.USER)
                                .emailVerified(true);
                        
                        // 소셜 ID 설정
                        if ("KAKAO".equals(provider)) {
                            userBuilder.kakaoId(Long.parseLong(socialId));
                        } else if ("NAVER".equals(provider)) {
                            userBuilder.naverId(socialId);
                        }
                        
                        return userRepository.save(userBuilder.build());
                    });
            
            // 3. JWT 토큰 생성
            String jwtToken = jwtTokenProvider.createToken(user.getEmail(), user.getId());
            
            // 사용자 정보 구성
            LoginResponseDTO.UserInfo socialUserInfo = new LoginResponseDTO.UserInfo(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                List.of(), // 알림 - 빈 배열
                List.of(), // 계약 - 빈 배열  
                List.of()  // 최근 채팅 - 빈 배열
            );
            
            LoginResponseDTO loginResponse = LoginResponseDTO.builder()
                    .token(jwtToken)
                    .user(socialUserInfo)
                    .build();
            
            return ApiResponse.onSuccess(loginResponse);
            
        } catch (IllegalArgumentException e) {
            log.error("임시 토큰 검증 실패", e);
            throw new RuntimeException("임시 토큰이 유효하지 않거나 만료되었습니다.", e);
        } catch (Exception e) {
            log.error("소셜 로그인 완료 처리 실패", e);
            throw new RuntimeException("소셜 로그인 완료 처리 중 오류가 발생했습니다.", e);
        }
    }
} 