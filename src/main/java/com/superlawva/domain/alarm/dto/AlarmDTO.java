// AlarmDTO.java - 수정됨: 클래스 이름 변경 및 구조 개선
package com.superlawva.domain.alarm.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import java.time.LocalDate;

/**
 * MongoDB 계약 데이터 매핑용 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlarmDTO {

    @JsonProperty("_id") // MongoDB의 _id 필드와 매핑
    private String id; // 계약 ID

    @JsonProperty("user_id") // MongoDB의 user_id 필드와 매핑
    private Long userId; // 사용자 ID

    @JsonProperty("contract_type") // MongoDB의 contract_type 필드와 매핑
    private String contractType; // 계약 유형 (전세, 월세 등)

    private ContractDates dates; // 계약 날짜 정보
    private PaymentInfo payment; // 결제 정보

    /**
     * 계약 날짜 정보 내부 클래스
     */
    @Getter
    @Setter
    @NoArgsConstructor
    public static class ContractDates {
        @JsonProperty("contract_date") // 계약 체결일 매핑
        @JsonFormat(pattern = "yyyy-MM-dd") // 날짜 형식 지정
        private LocalDate contractDate; // 계약 체결일

        @JsonProperty("start_date") // 계약 시작일 매핑
        @JsonFormat(pattern = "yyyy-MM-dd") // 날짜 형식 지정
        private LocalDate startDate; // 계약 시작일

        @JsonProperty("end_date") // 계약 종료일 매핑
        @JsonFormat(pattern = "yyyy-MM-dd") // 날짜 형식 지정
        private LocalDate endDate; // 계약 종료일
    }

    /**
     * 결제 정보 내부 클래스
     */
    @Getter
    @Setter
    @NoArgsConstructor
    public static class PaymentInfo {
        private Long deposit; // 보증금

        @JsonProperty("down_payment") // 계약금 매핑
        private Long downPayment; // 계약금

        @JsonProperty("intermediate_payment") // 중도금 매핑
        private Long intermediatePayment; // 중도금

        @JsonProperty("intermediate_payment_date") // 중도금 지급일 매핑
        private String intermediatePaymentDate; // 중도금 지급일

        @JsonProperty("remaining_balance") // 잔금 매핑
        private Long remainingBalance; // 잔금

        @JsonProperty("remaining_balance_date") // 잔금 지급일 매핑
        private String remainingBalanceDate; // 잔금 지급일

        @JsonProperty("monthly_rent") // 월세 매핑
        private Long monthlyRent; // 월세

        @JsonProperty("monthly_rent_date") // 월세 지급일 매핑
        private String monthlyRentDate; // 월세 지급일 ("1일", "말일", "20일" 등)
    }
}