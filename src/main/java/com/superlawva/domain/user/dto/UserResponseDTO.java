package com.superlawva.domain.user.dto;

import com.superlawva.domain.user.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Builder;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@Schema(description = "사용자 응답 DTO")
public class UserResponseDTO {
    
    @Schema(description = "사용자 ID", example = "1")
    private Long id;
    
    @Schema(description = "사용자 이메일", example = "test@example.com")
    private String email;
    
    @Schema(description = "사용자 이름", example = "홍길동")
    private String userName;  // 프론트엔드 호환을 위해 userName으로 변경
    
    @Schema(description = "사용자 역할", example = "USER")
    private User.Role role;
    
    @Schema(description = "가입일시", example = "2025-01-15T10:30:00")
    private LocalDateTime createdAt;
    
    @Schema(description = "이메일 인증 여부", example = "true")
    private boolean emailVerified;

    public static UserResponseDTO of(User user) {
        return UserResponseDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .userName(user.getNickname())  // nickname을 userName으로 매핑
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .emailVerified(user.isEmailVerified())
                .build();
    }

    public static UserResponseDTO from(User user) {
        return UserResponseDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .userName(user.getNickname())  // nickname을 userName으로 매핑
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .emailVerified(user.isEmailVerified())
                .build();
    }

    @Getter
    @Builder
    @Schema(description = "마이페이지 사용자 정보")
    public static class MyPageDTO {
        @Schema(description = "사용자 ID", example = "1")
        private Long id;
        
        @Schema(description = "사용자 이메일", example = "test@example.com")
        private String email;
        
        @Schema(description = "사용자 이름", example = "홍길동")
        private String userName;  // 프론트엔드 호환을 위해 userName 사용
    }
}
