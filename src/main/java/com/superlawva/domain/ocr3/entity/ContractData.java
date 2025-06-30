package com.superlawva.domain.ocr3.entity;

import jakarta.persistence.*;
import jakarta.persistence.GenerationType;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.superlawva.global.security.converter.AriaCryptoConverter;
import com.superlawva.global.security.converter.AesCryptoConverter;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "contract_data")
@Data
public class ContractData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "contract_type")
    private String contractType;

    @Embedded
    private Dates dates;
    
    @Embedded
    private Property property;
    
    @Embedded
    private Payment payment;
    
    @Lob
    @Column(name = "articles_json", columnDefinition = "TEXT")
    private String articlesJson; // List<String> → JSON String
    
    @Lob
    @Column(name = "agreements_json", columnDefinition = "TEXT") 
    private String agreementsJson; // List<String> → JSON String

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "name", column = @Column(name = "lessor_name")),
        @AttributeOverride(name = "idNumber", column = @Column(name = "lessor_id_number")),
        @AttributeOverride(name = "address", column = @Column(name = "lessor_address")),
        @AttributeOverride(name = "detailAddress", column = @Column(name = "lessor_detail_address")),
        @AttributeOverride(name = "phoneNumber", column = @Column(name = "lessor_phone_number")),
        @AttributeOverride(name = "mobileNumber", column = @Column(name = "lessor_mobile_number"))
    })
    private Party lessor;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "name", column = @Column(name = "lessee_name")),
        @AttributeOverride(name = "idNumber", column = @Column(name = "lessee_id_number")),
        @AttributeOverride(name = "address", column = @Column(name = "lessee_address")),
        @AttributeOverride(name = "detailAddress", column = @Column(name = "lessee_detail_address")),
        @AttributeOverride(name = "phoneNumber", column = @Column(name = "lessee_phone_number")),
        @AttributeOverride(name = "mobileNumber", column = @Column(name = "lessee_mobile_number"))
    })
    private Party lessee;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "office", column = @Column(name = "broker1_office")),
        @AttributeOverride(name = "licenseNumber", column = @Column(name = "broker1_license_number")),
        @AttributeOverride(name = "address", column = @Column(name = "broker1_address")),
        @AttributeOverride(name = "representative", column = @Column(name = "broker1_representative")),
        @AttributeOverride(name = "faoBroker", column = @Column(name = "broker1_fao_broker"))
    })
    private Broker broker1;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "office", column = @Column(name = "broker2_office")),
        @AttributeOverride(name = "licenseNumber", column = @Column(name = "broker2_license_number")),
        @AttributeOverride(name = "address", column = @Column(name = "broker2_address")),
        @AttributeOverride(name = "representative", column = @Column(name = "broker2_representative")),
        @AttributeOverride(name = "faoBroker", column = @Column(name = "broker2_fao_broker"))
    })
    private Broker broker2;

    @Column(name = "is_generated")
    private Boolean isGenerated;
    
    @Lob
    @Column(name = "recommended_agreements_json", columnDefinition = "TEXT")
    private String recommendedAgreementsJson;
    
    @Lob
    @Column(name = "legal_basis_json", columnDefinition = "TEXT")
    private String legalBasisJson;
    
    @Lob
    @Column(name = "case_basis_json", columnDefinition = "TEXT")
    private String caseBasisJson;
    
    @Lob
    @Column(name = "analysis_metadata_json", columnDefinition = "TEXT")
    private String analysisMetadataJson;

    @Column(name = "file_url")
    private String fileUrl;

    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @Column(name = "modified_date")
    private LocalDateTime modifiedDate;

    @Embedded
    private ContractMetadata contractMetadata;

    /* 전체 계약 JSON 원본 */
    @Convert(converter = AesCryptoConverter.class)
    @Lob
    @Column(name = "contract_json", columnDefinition = "LONGTEXT")
    private String contractJson;

    @Embeddable
    @Data
    public static class Dates {
        @Column(name = "contract_date")
        private String contractDate;

        @Column(name = "start_date")
        private String startDate;

        @Column(name = "end_date")
        private String endDate;
    }

    @Embeddable
    @Data
    public static class Property {
        @Column(name = "property_address")
        private String address;

        @Column(name = "property_detail_address")
        private String detailAddress;

        @Column(name = "rent_section")
        private String rentSection;

        @Column(name = "rent_area")
        private String rentArea;

        @Embedded
        @AttributeOverrides({
            @AttributeOverride(name = "landType", column = @Column(name = "land_type")),
            @AttributeOverride(name = "landRightRate", column = @Column(name = "land_right_rate")),
            @AttributeOverride(name = "landArea", column = @Column(name = "land_area"))
        })
        private Land land;
        
        @Embedded
        @AttributeOverrides({
            @AttributeOverride(name = "buildingConstructure", column = @Column(name = "building_constructure")),
            @AttributeOverride(name = "buildingType", column = @Column(name = "building_type")),
            @AttributeOverride(name = "buildingArea", column = @Column(name = "building_area"))
        })
        private Building building;

        @Embeddable
        @Data
        public static class Land {
            @Column(name = "land_type")
            private String landType;

            @Column(name = "land_right_rate")
            private String landRightRate;

            @Column(name = "land_area")
            private Double landArea;
        }

        @Embeddable
        @Data
        public static class Building {
            @Column(name = "building_constructure")
            private String buildingConstructure;

            @Column(name = "building_type")
            private String buildingType;

            @Column(name = "building_area")
            private String buildingArea;
        }
    }

    @Embeddable
    @Data
    public static class Payment {
        @Column(name = "deposit")
        private Long deposit;

        @Column(name = "deposit_kr")
        private String depositKr;

        @Column(name = "down_payment")
        private Long downPayment;

        @Column(name = "down_payment_kr")
        private String downPaymentKr;

        @Column(name = "intermediate_payment")
        private Long intermediatePayment;

        @Column(name = "intermediate_payment_kr")
        private String intermediatePaymentKr;

        @Column(name = "intermediate_payment_date")
        private String intermediatePaymentDate;

        @Column(name = "remaining_balance")
        private Long remainingBalance;

        @Column(name = "remaining_balance_kr")
        private String remainingBalanceKr;

        @Column(name = "remaining_balance_date")
        private String remainingBalanceDate;

        @Column(name = "monthly_rent")
        private Long monthlyRent;

        @Column(name = "monthly_rent_date")
        private String monthlyRentDate;

        @Column(name = "payment_plan")
        private String paymentPlan;
    }

    @Embeddable
    @Data
    public static class Party {
        private String name;

        @Convert(converter = AriaCryptoConverter.class)
        @Column(name = "id_number")
        private String idNumber;

        private String address;

        @Column(name = "detail_address")
        private String detailAddress;

        @Convert(converter = AriaCryptoConverter.class)
        private String phoneNumber;

        @Convert(converter = AriaCryptoConverter.class)
        private String mobileNumber;

        @Column(name = "agent_name", insertable = false, updatable = false)
        private String agentName;
    }

    @Embeddable
    @Data
    public static class Broker {
        private String office;

        @Convert(converter = AriaCryptoConverter.class)
        @Column(name = "license_number")
        private String licenseNumber;

        private String address;
        private String representative;

        @Column(name = "fao_broker")
        private String faoBroker;
    }

    @Embeddable
    @Data
    public static class ContractMetadata {
        @Column(name = "metadata_model")
        private String model;

        @Column(name = "generation_time")
        private Double generationTime;

        @Column(name = "user_agent")
        private String userAgent;

        @Column(name = "metadata_version")
        private String version;
    }
}