package com.superlawva.domain.user.controller;

import com.superlawva.domain.user.dto.UserRequestDTO;
import com.superlawva.domain.user.dto.UserResponseDTO;
import com.superlawva.domain.user.dto.LoginResponseDTO;
import com.superlawva.domain.user.dto.PasswordChangeRequestDTO;
import com.superlawva.domain.user.entity.User;
import com.superlawva.domain.user.service.UserService;
import com.superlawva.global.response.ApiResponse;
import com.superlawva.global.response.status.ErrorStatus;
import com.superlawva.global.security.annotation.LoginUser;
import com.superlawva.global.exception.BaseException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
@Tag(name = "👤 User Management", description = "사용자 정보 조회 및 관리 (🔒인증 필요)")
public class UserController {

    private final UserService userService;

    @GetMapping("/info")
    @Operation(
        summary = "👤 내 정보 조회 (마이페이지)", 
        description = """
        현재 로그인한 사용자의 상세 정보를 조회합니다.
        
        **제공 정보:**
        - 기본 정보: ID, 이메일, 닉네임
        - 계정 정보: 가입일, 수정일, 인증 상태
        - 로그인 방식: LOCAL, KAKAO, NAVER
        
        **사용법:**
        ```javascript
        const response = await fetch('/users/info', {
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        });
        const userInfo = await response.json();
        ```
        """
    )
    @SecurityRequirement(name = "JWT")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "정보 조회 성공", content = @Content(schema = @Schema(implementation = UserResponseDTO.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "사용자 정보를 찾을 수 없음")
    })
    public ApiResponse<UserResponseDTO> getMyInfo(@Parameter(hidden = true) @LoginUser User user) {
        if (user == null) {
            throw new BaseException(ErrorStatus._UNAUTHORIZED);
        }
        return ApiResponse.onSuccess(userService.getMyInfo(user.getId()));
    }

    @PostMapping("/dashboard")
    @Operation(
        summary = "📊 사용자 대시보드 정보 조회", 
        description = """
        프론트엔드 대시보드 화면을 위한 종합 정보를 조회합니다.
        
        **제공 정보:**
        - 사용자 기본 정보
        - 알림 현황 (읽지 않은 알림 개수)
        - 계약 정보 목록 (진행중/완료된 계약)
        - 최근 채팅 내역
        
        **사용 시나리오:**
        - 메인 대시보드 화면 로드
        - 사용자 활동 요약 정보 표시
        - 빠른 접근을 위한 최근 항목들
        """
    )
    @SecurityRequirement(name = "JWT")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "대시보드 정보 조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })
    public ApiResponse<LoginResponseDTO> getUserDashboard(@Parameter(hidden = true) @LoginUser User user) {
        if (user == null) {
            throw new BaseException(ErrorStatus._UNAUTHORIZED);
        }
        return ApiResponse.onSuccess(userService.getUserDashboard(user.getId()));
    }

    @PutMapping("/info")
    @Operation(
        summary = "✏️ 내 정보 수정", 
        description = """
        현재 로그인한 사용자의 프로필 정보를 수정합니다.
        
        **수정 가능한 정보:**
        - 닉네임 (표시 이름)
        - 기타 프로필 정보
        
        **사용법:**
        ```javascript
        const response = await fetch('/users/info', {
            method: 'PUT',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                nickname: "새로운닉네임"
            })
        });
        ```
        
        **주의사항:**
        - 이메일은 변경할 수 없습니다
        - 닉네임은 2-20자 사이여야 합니다
        """
    )
    @SecurityRequirement(name = "JWT")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "정보 수정 성공", content = @Content(schema = @Schema(implementation = UserResponseDTO.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "사용자 정보를 찾을 수 없음")
    })
    public ApiResponse<UserResponseDTO> updateMyInfo(@Parameter(hidden = true) @LoginUser User user, @RequestBody @Valid UserRequestDTO.UpdateMyInfoDTO request) {
        if (user == null) {
            throw new BaseException(ErrorStatus._UNAUTHORIZED);
        }
        return ApiResponse.onSuccess(userService.updateMyInfo(user.getId(), request));
    }

    @DeleteMapping("/me")
    @Operation(
        summary = "🗑️ 회원 탈퇴", 
        description = """
        현재 로그인한 사용자의 계정을 삭제 처리합니다.
        
        **주의사항:**
        - **이 작업은 되돌릴 수 없습니다**
        - 모든 사용자 데이터가 삭제됩니다
        - 관련된 계약, 알림, 채팅 기록도 함께 삭제됩니다
        
        **처리 과정:**
        1. 사용자 인증 확인
        2. 관련 데이터 삭제
        3. 계정 비활성화
        4. 자동 로그아웃 처리
        
        **사용법:**
        ```javascript
        if (confirm('정말로 탈퇴하시겠습니까?')) {
            const response = await fetch('/users/me', {
                method: 'DELETE',
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            });
            
            if (response.ok) {
                localStorage.removeItem('access_token');
                window.location.href = '/';
            }
        }
        ```
        """
    )
    @SecurityRequirement(name = "JWT")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "회원 탈퇴 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "사용자 정보를 찾을 수 없음")
    })
    public ApiResponse<Void> deleteMyAccount(@Parameter(hidden = true) @LoginUser User user) {
        if (user == null) {
            throw new BaseException(ErrorStatus._UNAUTHORIZED);
        }
        userService.deleteMyAccount(user.getId());
        return ApiResponse.onSuccess(null);
    }

    @PatchMapping("/me/password")
    @SecurityRequirement(name = "JWT")
    @Operation(
        summary = "🔐 내 비밀번호 변경", 
        description = """
        현재 로그인한 사용자의 비밀번호를 변경합니다.
        
        **요구사항:**
        - 현재 비밀번호 입력 필수
        - 새 비밀번호 확인 필요
        - 새 비밀번호는 기존과 달라야 함
        
        **요청 예시:**
        ```json
        {
            "currentPassword": "기존비밀번호",
            "newPassword": "새비밀번호123",
            "confirmPassword": "새비밀번호123"
        }
        ```
        
        **보안 특징:**
        - 기존 비밀번호 검증 후 변경
        - 새 비밀번호 암호화 저장
        - 변경 후 기존 세션 유지
        
        **주의사항:**
        - 소셜 로그인 사용자는 비밀번호가 없을 수 있습니다
        - 비밀번호 분실 시 비밀번호 재설정을 이용하세요
        """
    )
    public ApiResponse<String> changePassword(@Parameter(hidden = true) @LoginUser User user,
                                              @RequestBody @Valid PasswordChangeRequestDTO request) {
        userService.changePassword(user, request);
        return ApiResponse.onSuccess("비밀번호가 성공적으로 변경되었습니다.");
    }
    
    // ================================================================================================================
    // 아래 API들은 관리자(ADMIN) 권한이 필요하며, 현재는 데모 및 테스트 목적으로 열려있습니다.
    // 실제 운영 시에는 @PreAuthorize("hasRole('ADMIN')") 등을 사용하여 접근을 제한해야 합니다.
    // ================================================================================================================

    @Operation(summary = "전체 회원 조회 (관리자용)", description = "등록된 모든 회원 정보를 조회합니다.")
    @GetMapping
    @SecurityRequirement(name = "JWT")
    public ApiResponse<List<UserResponseDTO>> all() {
        return ApiResponse.onSuccess(userService.findAll());
    }

    @Operation(summary = "특정 회원 조회 (관리자용)", description = "ID를 이용하여 특정 회원 정보를 조회합니다.")
    @GetMapping("/{id}")
    @SecurityRequirement(name = "JWT")
    public ApiResponse<UserResponseDTO> one(@Parameter(description = "사용자 ID", in = ParameterIn.PATH) @PathVariable Long id) {
        return ApiResponse.onSuccess(userService.findById(id));
    }

    @Operation(summary = "회원 정보 수정 (관리자용)", description = "ID에 해당하는 회원 정보를 수정합니다.")
    @PutMapping("/{id}")
    @SecurityRequirement(name = "JWT")
    public ApiResponse<UserResponseDTO> update(@Parameter(description = "사용자 ID", in = ParameterIn.PATH) @PathVariable Long id,
                                  @RequestBody UserRequestDTO dto) {
        return ApiResponse.onSuccess(userService.update(id, dto));
    }

    @Operation(summary = "회원 삭제 (관리자용)", description = "ID에 해당하는 회원 정보를 삭제합니다.")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @SecurityRequirement(name = "JWT")
    public void delete(@Parameter(description = "사용자 ID", in = ParameterIn.PATH) @PathVariable Long id) {
        userService.delete(id);
    }
}
