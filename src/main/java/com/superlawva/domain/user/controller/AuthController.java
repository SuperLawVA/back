package com.superlawva.domain.user.controller;

// 기존 import들
import com.superlawva.domain.user.dto.LoginRequestDTO;
import com.superlawva.domain.user.dto.UserRequestDTO;
import com.superlawva.domain.user.dto.UserResponseDTO;
import com.superlawva.domain.user.dto.LoginResponseDTO; // 🆕 추가 - 새로 만든 응답 DTO
import com.superlawva.domain.user.entity.User; // 🔧 추가 누락 - User 엔티티 import
import com.superlawva.domain.user.service.UserService;
import com.superlawva.domain.user.repository.UserRepository; // 🆕 추가 - 사용자 데이터 조회용
import com.superlawva.global.common.ApiResponse;
import com.superlawva.global.security.util.JwtTokenProvider;
import com.superlawva.global.security.util.AESUtil; // 🆕 추가 - 사용자 ID 암호화용
import io.swagger.v3.oas.annotations.Operation; // 🆕 추가 - API 문서화
import io.swagger.v3.oas.annotations.tags.Tag;   // 🆕 추가 - API 문서화

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays; // 🆕 추가 - 배열을 리스트로 변환하기 위해
import java.util.List;   // 🔧 추가 누락 - List 인터페이스 import
import java.util.Map;

@RestController // REST API 컨트롤러임을 표시
@RequestMapping("/auth") // 이 컨트롤러의 기본 URL 경로 설정
@Tag(name = "Authentication", description = "인증 관련 API") // 🆕 추가 - Swagger 문서화를 위한 태그
public class AuthController {

    // 기존 의존성들
    private final AuthenticationManager authenticationManager; // 사용자 인증 처리
    private final UserService userService; // 사용자 관련 비즈니스 로직
    private final JwtTokenProvider jwtTokenProvider; // JWT 토큰 생성/검증

    // 🆕 추가된 의존성들
    private final UserRepository userRepository; // 사용자 데이터베이스 조회
    private final AESUtil aesUtil; // 사용자 ID 암호화 처리

    // 🆕 수정된 생성자 - 새로운 의존성들 추가
    public AuthController(AuthenticationManager authenticationManager,
                          UserService userService,
                          JwtTokenProvider jwtTokenProvider,
                          UserRepository userRepository, // 🆕 추가
                          AESUtil aesUtil) {             // 🆕 추가
        this.authenticationManager = authenticationManager; // 인증 매니저 주입
        this.userService           = userService;           // 사용자 서비스 주입
        this.jwtTokenProvider      = jwtTokenProvider;      // JWT 제공자 주입
        this.userRepository        = userRepository;        // 🆕 사용자 레포지토리 주입
        this.aesUtil              = aesUtil;               // 🆕 AES 암호화 유틸 주입
    }

    @PostMapping("/signup")
    @Operation(summary = "회원가입", description = "새로운 사용자를 등록합니다.")
    public ResponseEntity<ApiResponse<UserResponseDTO>> signup(@RequestBody UserRequestDTO dto) {
        try {
            UserResponseDTO resp = userService.create(dto);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("회원가입이 완료되었습니다.", resp));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("회원가입에 실패했습니다: " + e.getMessage()));
        }
    }

    @PostMapping("/login")
    @Operation(summary = "기본 로그인", description = "로그인하여 JWT 토큰만 반환합니다.")
    public ResponseEntity<ApiResponse<Map<String, String>>> login(@RequestBody LoginRequestDTO dto) {
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(dto.getEmail(), dto.getPassword())
            );
            String token = jwtTokenProvider.createToken(dto.getEmail());
            return ResponseEntity.ok(ApiResponse.success("로그인에 성공했습니다.", Map.of("token", token)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("로그인에 실패했습니다. 이메일과 비밀번호를 확인해주세요.", "LOGIN_FAILED"));
        }
    }

    @PostMapping("/login/userinfo")
    @Operation(summary = "유저 정보 로그인", description = "로그인하여 유저 정보와 함께 반환합니다.")
    public ResponseEntity<ApiResponse<LoginResponseDTO>> loginWithUserInfo(@RequestBody LoginRequestDTO dto) {
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(dto.getEmail(), dto.getPassword())
            );

            String token = jwtTokenProvider.createToken(dto.getEmail());

            User user = userRepository.findByEmail(dto.getEmail())
                    .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

            LoginResponseDTO response = LoginResponseDTO.builder()
                    .message("Login success")
                    .userName(user.getNickname())
                    .userId(user.getId().toString()) // ✅ 암호화 없이 평문 userId
                    .JWTtoken(token)
                    .notification(getNotificationCounts(user.getId()))
                    .contract(getRecentContract(user.getId()))
                    .recentChat(getRecentChats(user.getId()))
                    .build();

            return ResponseEntity.ok(ApiResponse.success("로그인에 성공했습니다.", response));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("로그인에 실패했습니다. 이메일과 비밀번호를 확인해주세요.", "LOGIN_FAILED"));
        }
    }


    // 🆕 알림 개수 조회 메서드 (하드코딩)
    private List<Integer> getNotificationCounts(Long userId) {
        try {
            // TODO: 나중에 실제 알림 테이블 구현되면 아래와 같이 변경
            // int unread = alarmRepository.countByUserIdAndIsReadFalse(userId);
            // int urgent = alarmRepository.countUrgentByUserId(userId);
            // int total = alarmRepository.countByUserId(userId);
            // return Arrays.asList(unread, urgent, total);

            // 🆕 현재는 하드코딩된 알림 개수 반환 [읽지않은, 긴급, 전체]
            return Arrays.asList(0, 1, 2);
        } catch (Exception e) {
            // 오류 발생 시 모든 알림 개수를 0으로 반환
            return Arrays.asList(0, 0, 0);
        }
    }

    // 🆕 계약 정보 조회 메서드 (하드코딩)
    private LoginResponseDTO.ContractInfo getRecentContract(Long userId) {
        try {
            // 🆕 하드코딩된 계약 정보 반환 (MongoDB 스키마 기반 실제 데이터 형태)
            return LoginResponseDTO.ContractInfo.builder()
                    .title("월세 임대차 계약서") // 계약 유형 + "임대차 계약서"
                    .state("진행중") // 계약 상태 (진행중/만료)
                    .address("서울시 강남구 테헤란로 123") // 부동산 주소
                    .createdAt("2025.03.22") // 계약 생성일 (yyyy.MM.dd 형식)
                    .build(); // 빌더 패턴으로 객체 생성
        } catch (Exception e) {
            // 오류 발생 시 기본값 반환
            return LoginResponseDTO.ContractInfo.builder()
                    .title("임대차 계약서") // 기본 제목
                    .state("정보 없음") // 기본 상태
                    .address("계약 정보를 불러올 수 없습니다") // 기본 주소
                    .createdAt("정보 없음") // 기본 생성일
                    .build();
        }
    }

    // 🆕 최근 채팅 목록 조회 메서드 (하드코딩)
    private List<LoginResponseDTO.RecentChat> getRecentChats(Long userId) {
        try {
            // TODO: 나중에 실제 채팅 API 구현되면 아래와 같이 변경
            // String chatApiUrl = "/api/chats/recent/" + userId;
            // 실제 채팅 데이터 조회 로직

            // 🆕 현재는 하드코딩된 채팅 목록 반환
            return Arrays.asList(
                    LoginResponseDTO.RecentChat.builder()
                            ._id("1") // 첫 번째 채팅 ID
                            .title("제목1") // 첫 번째 채팅 제목
                            .build(),
                    LoginResponseDTO.RecentChat.builder()
                            ._id("2") // 두 번째 채팅 ID
                            .title("제목2") // 두 번째 채팅 제목
                            .build(),
                    LoginResponseDTO.RecentChat.builder()
                            ._id("3") // 세 번째 채팅 ID
                            .title("제목3") // 세 번째 채팅 제목
                            .build()
            );
        } catch (Exception e) {
            // 오류 발생 시 빈 채팅 목록 반환
            return Arrays.asList();
        }
    }
}