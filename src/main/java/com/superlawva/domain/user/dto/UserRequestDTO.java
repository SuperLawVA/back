package com.superlawva.domain.user.dto;

import com.superlawva.domain.user.entity.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import io.swagger.v3.oas.annotations.media.Schema;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "사용자 요청 DTO")
public class UserRequestDTO {

    @NotBlank(message = "닉네임은 필수 입력 항목입니다.")
    @Schema(description = "사용자 닉네임", example = "홍길동", requiredMode = Schema.RequiredMode.REQUIRED)
    private String nickname;

    @NotBlank(message = "이메일은 필수 입력 항목입니다.")
    @Email(message = "유효한 이메일 주소를 입력해주세요.")
    @Schema(description = "사용자 이메일", example = "test@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    @NotBlank(message = "비밀번호는 필수 입력 항목입니다.")
    @Schema(description = "사용자 비밀번호", example = "password123", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;

    public User toEntity() {
        return User.builder()
                .nickname(nickname)
                .email(email)
                .password(password)
                .build();
    }

    @Getter
    @Setter
    public static class SignUpDTO {
        @NotBlank(message = "닉네임은 필수 입력 항목입니다.")
        @Schema(description = "사용자 닉네임", example = "홍길동")
        private String nickname;

        @NotBlank(message = "이메일은 필수 입력 항목입니다.")
        @Email(message = "유효한 이메일 주소를 입력해주세요.")
        private String email;

        @NotBlank(message = "비밀번호는 필수 입력 항목입니다.")
        private String password;

        @NotBlank(message = "비밀번호 확인은 필수 입력 항목입니다.")
        private String confirmPassword;
    }

    @Getter
    @Schema(description = "내 정보 수정을 위한 요청 DTO")
    public static class UpdateMyInfoDTO {
        @NotBlank(message = "닉네임은 필수입니다.")
        @Schema(description = "새로운 닉네임", example = "새로운닉네임")
        private String nickname;
    }
}
