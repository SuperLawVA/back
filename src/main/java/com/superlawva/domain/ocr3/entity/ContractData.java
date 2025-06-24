package com.superlawva.domain.ocr3.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Document(collection = "contract")
public class ContractData {
    @Id
    private String id;
    
    @Field("user_id")
    private String userId;
    
    @Field("contract_type")
    private String contractType;
    
    private Dates dates;
    private Property property;
    private Payment payment;
    private List<String> articles;
    private List<String> agreements;
    private Party lessor;
    private Party lessee;
    private Broker broker1;
    private Broker broker2;
    
    private boolean generated;
    
    @Field("file_url")
    private String fileUrl;
    
    @Field("created_date")
    private LocalDateTime createdDate;
    
    @Field("modified_date")
    private LocalDateTime modifiedDate;
    
    @Field("contract_metadata")
    private ContractMetadata contractMetadata;
    
    @Data
    public static class Dates {
        @Field("contract_date")
        private String contractDate;
        
        @Field("start_date")
        private String startDate;
        
        @Field("end_date")
        private String endDate;
    }
    
    @Data
    public static class Property {
        private String address;
        
        @Field("detail_address")
        private String detailAddress;
        
        @Field("rent_section")
        private String rentSection;
        
        @Field("rent_area")
        private String rentArea;
        
        private Land land;
        private Building building;
        
        @Data
        public static class Land {
            @Field("land_type")
            private String landType;
            
            @Field("land_right_rate")
            private String landRightRate;
            
            @Field("land_area")
            private Double landArea;
        }
        
        @Data
        public static class Building {
            @Field("building_constructure")
            private String buildingConstructure;
            
            @Field("building_type")
            private String buildingType;
            
            @Field("building_area")
            private String buildingArea;
        }
    }
    
    @Data
    public static class Payment {
        private Long deposit;
        
        @Field("deposit_kr")
        private String depositKr;
        
        @Field("down_payment")
        private Long downPayment;
        
        @Field("down_payment_kr")
        private String downPaymentKr;
        
        @Field("intermediate_payment")
        private Long intermediatePayment;
        
        @Field("intermediate_payment_kr")
        private String intermediatePaymentKr;
        
        @Field("intermediate_payment_date")
        private String intermediatePaymentDate;
        
        @Field("remaining_balance")
        private Long remainingBalance;
        
        @Field("remaining_balance_kr")
        private String remainingBalanceKr;
        
        @Field("remaining_balance_date")
        private String remainingBalanceDate;
        
        @Field("monthly_rent")
        private Long monthlyRent;
        
        @Field("monthly_rent_date")
        private String monthlyRentDate;
        
        @Field("payment_plan")
        private String paymentPlan;
    }
    
    @Data
    public static class Party {
        private String name;
        
        @Field("id_number")
        private String idNumber;
        
        private String address;
        
        @Field("detail_address")
        private String detailAddress;
        
        @Field("phone_number")
        private String phoneNumber;
        
        @Field("mobile_number")
        private String mobileNumber;
        
        private Agent agent;
        
        @Data
        public static class Agent {
            private String name;
        }
    }
    
    @Data
    public static class Broker {
        private String office;
        
        @Field("license_number")
        private String licenseNumber;
        
        private String address;
        private String representative;
        
        @Field("fao_broker")
        private String faoBroker;
    }
    
    @Data
    public static class ContractMetadata {
        private String model;
        
        @Field("generation_time")
        private Double generationTime;
        
        @Field("user_agent")
        private String userAgent;
        
        private String version;
    }
}