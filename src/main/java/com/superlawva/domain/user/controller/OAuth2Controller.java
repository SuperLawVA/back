package com.superlawva.domain.user.controller;

import com.superlawva.domain.user.dto.KakaoLoginRequestDTO;
import com.superlawva.domain.user.dto.LoginResponseDTO;
import com.superlawva.domain.user.dto.NaverLoginRequestDTO;
import com.superlawva.domain.user.dto.SocialLoginTempDTO;
import com.superlawva.domain.user.entity.User;
import com.superlawva.domain.user.repository.UserRepository;
import com.superlawva.domain.user.service.UserService;
import com.superlawva.global.response.ApiResponse;
import com.superlawva.global.security.util.JwtTokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/auth/oauth2")
@RequiredArgsConstructor
@Tag(name = "🔐 OAuth2 Authentication", description = "소셜 로그인 API")
public class OAuth2Controller {

    private final UserService userService;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${spring.security.oauth2.client.registration.kakao.client-id}")
    private String kakaoClientId;

    @Value("${spring.security.oauth2.client.registration.kakao.client-secret}")
    private String kakaoClientSecret;

    @Value("${spring.security.oauth2.client.registration.kakao.redirect-uri}")
    private String kakaoRedirectUri;

    @Value("${spring.security.oauth2.client.registration.naver.client-id}")
    private String naverClientId;

    @Value("${spring.security.oauth2.client.registration.naver.client-secret}")
    private String naverClientSecret;

    @Value("${spring.security.oauth2.client.registration.naver.redirect-uri}")
    private String naverRedirectUri;

    @PostMapping("/login/kakao")
    @Operation(
        summary = "📱 카카오 소셜 로그인 (인가 코드 방식)", 
        description = """
        카카오 인가 코드를 받아 로그인 처리 후 JWT 토큰을 발급합니다.
        
        **사용법:**
        1. 프론트엔드에서 카카오 SDK를 사용하여 인가 코드를 받습니다
        2. 받은 인가 코드를 이 API로 전송합니다
        3. 성공 시 JWT 토큰과 사용자 정보를 반환합니다
        """
    )
    public ApiResponse<LoginResponseDTO> kakaoLogin(@RequestBody @Valid KakaoLoginRequestDTO request) {
        return ApiResponse.onSuccess(userService.kakaoLogin(request));
    }

    @PostMapping("/login/naver")
    @Operation(
        summary = "📱 네이버 소셜 로그인 (인가 코드 방식)", 
        description = """
        네이버 인가 코드를 받아 로그인 처리 후 JWT 토큰을 발급합니다.
        
        **사용법:**
        1. 프론트엔드에서 네이버 SDK를 사용하여 인가 코드를 받습니다
        2. 받은 인가 코드와 state를 이 API로 전송합니다
        3. 성공 시 JWT 토큰과 사용자 정보를 반환합니다
        """
    )
    public ApiResponse<LoginResponseDTO> naverLogin(@RequestBody @Valid NaverLoginRequestDTO request) {
        return ApiResponse.onSuccess(userService.naverLogin(request));
    }

    @GetMapping("/kakao")
    @Operation(
        summary = "🔗 카카오 OAuth2 인증 URL 생성", 
        description = """
        카카오 OAuth2 인증을 위한 URL을 생성합니다.
        
        **사용법:**
        1. 이 API를 호출하여 카카오 인증 URL을 받습니다
        2. 받은 URL로 사용자를 리다이렉트합니다
        3. 사용자가 카카오 로그인을 완료하면 `/auth/oauth2/callback/kakao`로 콜백됩니다
        """
    )
    public ApiResponse<Map<String, String>> getKakaoAuthUrl(@RequestParam(required = false) String state) {
        try {
            String authUrl = UriComponentsBuilder
                    .fromUriString("https://kauth.kakao.com/oauth/authorize")
                    .queryParam("client_id", kakaoClientId)
                    .queryParam("redirect_uri", kakaoRedirectUri)
                    .queryParam("response_type", "code")
                    .queryParam("scope", "profile_nickname")
                    .build()
                    .toUriString();

            Map<String, String> result = new HashMap<>();
            result.put("authUrl", authUrl);
            
            return ApiResponse.onSuccess(result);
        } catch (Exception e) {
            log.error("카카오 인증 URL 생성 실패", e);
            throw new RuntimeException("카카오 인증 URL 생성에 실패했습니다.");
        }
    }

    @GetMapping("/naver")
    @Operation(
        summary = "🔗 네이버 OAuth2 인증 URL 생성", 
        description = """
        네이버 OAuth2 인증을 위한 URL을 생성합니다.
        
        **사용법:**
        1. 이 API를 호출하여 네이버 인증 URL을 받습니다
        2. 받은 URL로 사용자를 리다이렉트합니다
        3. 사용자가 네이버 로그인을 완료하면 `/auth/oauth2/callback/naver`로 콜백됩니다
        """
    )
    public ApiResponse<Map<String, String>> getNaverAuthUrl(@RequestParam(required = false) String state) {
        try {
            // 상태값이 없으면 랜덤 생성
            if (state == null || state.isEmpty()) {
                state = java.util.UUID.randomUUID().toString();
            }
            
            String authUrl = UriComponentsBuilder
                    .fromUriString("https://nid.naver.com/oauth2.0/authorize")
                    .queryParam("client_id", naverClientId)
                    .queryParam("redirect_uri", naverRedirectUri)
                    .queryParam("response_type", "code")
                    .queryParam("state", state)
                    .build()
                    .toUriString();

            Map<String, String> result = new HashMap<>();
            result.put("authUrl", authUrl);
            result.put("state", state);
            
            return ApiResponse.onSuccess(result);
        } catch (Exception e) {
            log.error("네이버 인증 URL 생성 실패", e);
            throw new RuntimeException("네이버 인증 URL 생성에 실패했습니다.");
        }
    }

    @GetMapping("/callback/kakao")
    @Operation(
        summary = "🔄 카카오 OAuth2 콜백 처리", 
        description = """
        카카오 OAuth2 인증 후 콜백을 처리합니다.
        
        **동작 과정:**
        1. 카카오에서 인가 코드(code)를 받습니다
        2. 인가 코드로 카카오 액세스 토큰을 요청합니다
        3. 액세스 토큰으로 카카오 사용자 정보를 조회합니다
        4. 임시 토큰을 발급하고 이메일 입력 요청
        """
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "⏳ 이메일 입력 필요 (임시 토큰 발급)",
            content = @Content(
                schema = @Schema(implementation = SocialLoginTempDTO.class),
                examples = @ExampleObject(
                    value = """
                    {
                        "isSuccess": true,
                        "code": "200",
                        "message": "요청에 성공했습니다.",
                        "result": {
                            "tempToken": "temp_token_abc123",
                            "nickname": "카카오사용자",
                            "provider": "KAKAO",
                            "needEmail": true,
                            "message": "카카오 로그인이 완료되었습니다. 이메일을 입력해주세요."
                        }
                    }
                    """
                )
            )
        )
    })
    public ApiResponse<Object> kakaoCallback(@RequestParam String code) {
        try {
            // 1. 인가 코드로 액세스 토큰 요청
            String tokenUrl = "https://kauth.kakao.com/oauth/token";
            
            HttpHeaders tokenHeaders = new HttpHeaders();
            tokenHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            
            String tokenBody = String.format(
                "grant_type=authorization_code&client_id=%s&client_secret=%s&redirect_uri=%s&code=%s",
                kakaoClientId, kakaoClientSecret, kakaoRedirectUri, code
            );
            
            HttpEntity<String> tokenRequest = new HttpEntity<>(tokenBody, tokenHeaders);
            ResponseEntity<Map> tokenResponse = restTemplate.postForEntity(tokenUrl, tokenRequest, Map.class);
            
            String accessToken = (String) tokenResponse.getBody().get("access_token");
            if (accessToken == null) {
                throw new RuntimeException("카카오 액세스 토큰을 받지 못했습니다.");
            }
            
            // 2. 액세스 토큰으로 사용자 정보 조회
            String userInfoUrl = "https://kapi.kakao.com/v2/user/me";
            
            HttpHeaders userHeaders = new HttpHeaders();
            userHeaders.setBearerAuth(accessToken);
            
            HttpEntity<String> userRequest = new HttpEntity<>(userHeaders);
            ResponseEntity<Map> userResponse = restTemplate.exchange(userInfoUrl, HttpMethod.GET, userRequest, Map.class);
            
            Map<String, Object> userInfo = userResponse.getBody();
            Long kakaoId = ((Number) userInfo.get("id")).longValue();
            
            Map<String, Object> properties = (Map<String, Object>) userInfo.get("properties");
            String nickname = (String) properties.get("nickname");
            
            // 3. 임시 토큰 생성 (이메일 입력 필요)
            String tempToken = jwtTokenProvider.createTempToken(kakaoId.toString(), "KAKAO", nickname);
            
            SocialLoginTempDTO result = SocialLoginTempDTO.builder()
                    .tempToken(tempToken)
                    .nickname(nickname)
                    .provider("KAKAO")
                    .needEmail(true)
                    .message("카카오 로그인이 완료되었습니다. 이메일을 입력해주세요.")
                    .build();
            
            return ApiResponse.onSuccess(result);
            
        } catch (Exception e) {
            log.error("카카오 콜백 처리 실패", e);
            throw new RuntimeException("카카오 로그인 처리 중 오류가 발생했습니다.", e);
        }
    }

    @GetMapping("/callback/naver")
    @Operation(
        summary = "🔄 네이버 OAuth2 콜백 처리", 
        description = """
        네이버 OAuth2 인증 후 콜백을 처리하고 JWT 토큰을 발급합니다.
        
        **동작 과정:**
        1. 네이버에서 인가 코드(code)와 state를 받습니다
        2. 인가 코드로 네이버 액세스 토큰을 요청합니다
        3. 액세스 토큰으로 네이버 사용자 정보를 조회합니다
        4. 사용자 정보로 JWT 토큰을 생성하여 반환합니다
        """
    )
    public ApiResponse<LoginResponseDTO> naverCallback(@RequestParam String code, @RequestParam String state) {
        try {
            // 1. 인가 코드로 액세스 토큰 요청
            String tokenUrl = UriComponentsBuilder
                    .fromUriString("https://nid.naver.com/oauth2.0/token")
                    .queryParam("grant_type", "authorization_code")
                    .queryParam("client_id", naverClientId)
                    .queryParam("client_secret", naverClientSecret)
                    .queryParam("code", code)
                    .queryParam("state", state)
                    .build()
                    .toUriString();
            
            ResponseEntity<Map> tokenResponse = restTemplate.postForEntity(tokenUrl, null, Map.class);
            String accessToken = (String) tokenResponse.getBody().get("access_token");
            
            if (accessToken == null) {
                throw new RuntimeException("네이버 액세스 토큰을 받지 못했습니다.");
            }
            
            // 2. 액세스 토큰으로 사용자 정보 조회
            String userInfoUrl = "https://openapi.naver.com/v1/nid/me";
            
            HttpHeaders userHeaders = new HttpHeaders();
            userHeaders.setBearerAuth(accessToken);
            
            HttpEntity<String> userRequest = new HttpEntity<>(userHeaders);
            ResponseEntity<Map> userResponse = restTemplate.exchange(userInfoUrl, HttpMethod.GET, userRequest, Map.class);
            
            Map<String, Object> userInfo = userResponse.getBody();
            Map<String, Object> response = (Map<String, Object>) userInfo.get("response");
            
            String email = (String) response.get("email");
            String name = (String) response.get("name");
            
            // 3. 사용자 정보로 JWT 토큰 생성
            User user = userRepository.findByEmail(email)
                    .orElseGet(() -> {
                        return userRepository.save(User.builder()
                                .email(email)
                                .nickname(name)
                                .provider("NAVER")
                                .role(User.Role.USER)
                                .emailVerified(true)
                                .build());
                    });
            
            String jwtToken = jwtTokenProvider.createToken(user.getEmail(), user.getId());
            
            // 사용자 정보 구성
            LoginResponseDTO.UserInfo userInfoDto = new LoginResponseDTO.UserInfo(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                List.of(), // 알림 - 빈 배열
                List.of(), // 계약 - 빈 배열  
                List.of()  // 최근 채팅 - 빈 배열
            );
            
            LoginResponseDTO loginResponse = LoginResponseDTO.builder()
                    .token(jwtToken)
                    .user(userInfoDto)
                    .build();
            
            return ApiResponse.onSuccess(loginResponse);
            
        } catch (Exception e) {
            log.error("네이버 콜백 처리 실패", e);
            throw new RuntimeException("네이버 로그인 처리 중 오류가 발생했습니다.", e);
        }
    }
} 