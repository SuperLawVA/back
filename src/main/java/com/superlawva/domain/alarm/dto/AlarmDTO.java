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
    @JsonProperty("_id")
    private String id;
    
    @JsonProperty("user_id")
    private Long userId;
    
    @JsonProperty("contract_type")
    private String contractType;
    
    private ContractDates dates;
    private PaymentInfo payment;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ContractDates {
        @JsonProperty("contract_date")
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate contractDate;
        
        @JsonProperty("start_date")
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate startDate;
        
        @JsonProperty("end_date")
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate endDate;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PaymentInfo {
        private Long deposit;
        
        @JsonProperty("down_payment")
        private Long downPayment;
        
        @JsonProperty("intermediate_payment")
        private Long intermediatePayment;
        
        @JsonProperty("intermediate_payment_date")
        private String intermediatePaymentDate;
        
        @JsonProperty("remaining_balance")
        private Long remainingBalance;
        
        @JsonProperty("remaining_balance_date")
        private String remainingBalanceDate;
        
        @JsonProperty("monthly_rent")
        private Long monthlyRent;
        
        @JsonProperty("monthly_rent_date")
        private String monthlyRentDate;
        
        public Long getIntermediatePayment() {
            return intermediatePayment;
        }
        
        public String getIntermediatePaymentDate() {
            return intermediatePaymentDate;
        }
        
        public Long getRemainingBalance() {
            return remainingBalance;
        }
        
        public String getRemainingBalanceDate() {
            return remainingBalanceDate;
        }
        
        public Long getMonthlyRent() {
            return monthlyRent;
        }
        
        public String getMonthlyRentDate() {
            return monthlyRentDate;
        }
    }
    
    public String getId() {
        return id;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public ContractDates getDates() {
        return dates;
    }
    
    public PaymentInfo getPayment() {
        return payment;
    }
} 