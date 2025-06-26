package com.superlawva.domain.chatbot.dto;

import com.superlawva.domain.chatbot.entity.ChatSessionEntity;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "채팅 세션 정보 응답")
public record ChatSessionResponseDTO(
        @Schema(description = "세션 ID")
        String sessionId,

        @Schema(description = "세션 생성 사용자 정보")
        UserInSessionDTO user,

        @Schema(description = "세션 생성 시각")
        LocalDateTime createdAt,

        @Schema(description = "세션 마지막 활동 시각")
        LocalDateTime lastActiveAt,

        @Schema(description = "세션 상태")
        String status
) {
    public static ChatSessionResponseDTO fromEntity(ChatSessionEntity entity) {
        UserInSessionDTO userInSessionDTO = (entity.getUser() != null)
                ? UserInSessionDTO.fromEntity(entity.getUser())
                : null;

        return new ChatSessionResponseDTO(
                entity.getSessionId(),
                userInSessionDTO,
                entity.getCreatedAt(),
                entity.getLastActiveAt(),
                entity.getStatus().name()
        );
    }

    @Schema(description = "세션 내 사용자 정보")
    public record UserInSessionDTO(
            @Schema(description = "사용자 ID")
            Long id,

            @Schema(description = "사용자 이메일")
            String email,
            
            @Schema(description = "사용자 닉네임")
            String nickname
    ) {
        public static UserInSessionDTO fromEntity(com.superlawva.domain.user.entity.User userEntity) {
            if (userEntity == null) {
                return null;
            }
            return new UserInSessionDTO(
                    userEntity.getId(),
                    userEntity.getEmail(),
                    userEntity.getNickname()
            );
        }
    }
} 