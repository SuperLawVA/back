// 🆕 LoginResponseDTO.java - 새로 추가된 파일
package com.superlawva.domain.user.dto;

import lombok.Builder; // Lombok Builder 패턴 사용을 위한 import
import lombok.Getter;  // Lombok Getter 자동 생성을 위한 import
import java.util.List; // List 컬렉션 사용을 위한 import

/**
 * 로그인 응답 DTO - 프론트엔드에 반환할 데이터 구조 정의
 */
@Getter  // 🆕 모든 필드에 대해 getter 메서드 자동 생성
@Builder // 🆕 빌더 패턴으로 객체 생성 가능하게 함
public class LoginResponseDTO {
    private String message;           // 🆕 로그인 성공 메시지 ("Login success")
    private String userName;          // 🆕 사용자 이름 (실제 User 엔티티에서 가져옴)
    private String userId;            // 🆕 보안상 암호화된 사용자 ID
    private String JWTtoken;          // 🆕 JWT 인증 토큰
    private List<Integer> notification; // 🆕 알림 개수 배열 [읽지않은, 긴급, 전체]
    private ContractInfo contract;    // 🆕 최근 계약 정보 객체
    private List<RecentChat> recentChat; // 🆕 최근 채팅 목록

    /**
     * 계약 정보 내부 클래스 - 계약서 관련 데이터 구조
     */
    @Getter  // 🆕 계약 정보 필드들의 getter 자동 생성
    @Builder // 🆕 계약 정보 객체도 빌더 패턴으로 생성 가능
    public static class ContractInfo {
        private String title;     // 🆕 계약 제목 (예: "전세 임대차 계약서")
        private String state;     // 🆕 계약 상태 (예: "진행중", "만료")
        private String address;   // 🆕 계약 부동산 주소
        private String createdAt; // 🆕 계약 생성일 (yyyy.MM.dd 형식)
    }

    /**
     * 최근 채팅 내부 클래스 - 채팅 데이터 구조
     */
    @Getter  // 🆕 채팅 정보 필드들의 getter 자동 생성
    @Builder // 🆕 채팅 정보 객체도 빌더 패턴으로 생성 가능
    public static class RecentChat {
        private String _id;   // 🆕 채팅 고유 ID
        private String title; // 🆕 채팅 제목
    }
}