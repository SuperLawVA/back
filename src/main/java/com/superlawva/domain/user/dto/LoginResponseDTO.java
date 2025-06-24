package com.superlawva.domain.user.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "로그인 응답 DTO")
@JsonInclude(JsonInclude.Include.NON_NULL)  // null 필드는 JSON에 포함하지 않음
public class LoginResponseDTO {

    @Schema(description = "발급된 JWT Access Token", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String token;

    @Schema(description = "사용자 정보")
    private UserInfo user;

    @Schema(description = "사용자 정보")
    public static class UserInfo {
        @Schema(description = "사용자 ID(고유번호)", example = "1")
        public Long id;

        @Schema(description = "사용자 이메일", example = "test@example.com")
        public String email;

        @Schema(description = "사용자 이름", example = "아무개")
        public String userName;

        @Schema(description = "알림 정보", example = "[0, 1, 2]")
        public List<Integer> notification;

        @Schema(description = "계약 정보 목록")
        public List<ContractInfo> contractArray;

        @Schema(description = "최근 채팅 정보 목록")
        public List<RecentChat> recentChat;

        public UserInfo(Long id, String email, String userName, List<Integer> notification, 
                       List<ContractInfo> contractArray, List<RecentChat> recentChat) {
            this.id = id;
            this.email = email;
            this.userName = userName;
            this.notification = notification;
            this.contractArray = contractArray;
            this.recentChat = recentChat;
        }
    }

    @Schema(description = "계약 정보")
    public record ContractInfo(
            @JsonProperty("_id")
            @Schema(description = "계약 ID", example = "contract_123")
            String _id,
            
            @Schema(description = "계약명", example = "월세 임대차 계약서")
            String title,
            
            @Schema(description = "계약 상태", example = "진행중")
            String state,
            
            @Schema(description = "주소", example = "서울시 강남구 테헤란로 123")
            String address,
            
            @Schema(description = "생성일", example = "2025.03.22")
            String createdAt,

            @Schema(description = "수정일", example = "2025.03.22")
            String modifiedAt
    ) {}

    @Schema(description = "최근 채팅 정보")
    public record RecentChat(
            @JsonProperty("_id")
            @Schema(description = "채팅방 ID", example = "chat_001")
            String _id,
            
            @Schema(description = "채팅방 제목", example = "집 주인이 보증금 안 돌려줘요.")
            String title
    ) {}
} 