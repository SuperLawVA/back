package com.superlawva.domain.ml.entity;

import jakarta.persistence.*;
import jakarta.persistence.GenerationType;
import lombok.*;
import java.time.LocalDateTime;
import com.superlawva.global.security.converter.AesCryptoConverter;

@Entity
@Table(name = "certificates")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CertificateEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ml_certificate_id")
    private Integer mlCertificateId; // ML API에서 반환하는 ID

    @Column(name = "contract_id")
    private String contractId;

    @Column(name = "user_id")
    private String userId;

    @Convert(converter = AesCryptoConverter.class)
    @Lob
    @Column(name = "user_query", columnDefinition = "TEXT")
    private String userQuery;

    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @Column(name = "ml_created_date")
    private String mlCreatedDate; // ML API에서 반환하는 생성일 (원본 형식 보존)

    @Column(name = "title")
    private String title;

    @Convert(converter = AesCryptoConverter.class)
    @Lob
    @Column(name = "receiver_json", columnDefinition = "TEXT")
    private String receiverJson; // JSON 문자열로 저장

    @Convert(converter = AesCryptoConverter.class)
    @Lob
    @Column(name = "sender_json", columnDefinition = "TEXT")
    private String senderJson; // JSON 문자열로 저장

    @Convert(converter = AesCryptoConverter.class)
    @Lob
    @Column(name = "body", columnDefinition = "TEXT")
    private String body;

    @Convert(converter = AesCryptoConverter.class)
    @Lob
    @Column(name = "strategy_summary", columnDefinition = "TEXT")
    private String strategySummary;

    @Convert(converter = AesCryptoConverter.class)
    @Lob
    @Column(name = "followup_strategy", columnDefinition = "TEXT")
    private String followupStrategy;

    @Convert(converter = AesCryptoConverter.class)
    @Lob
    @Column(name = "legal_basis_json", columnDefinition = "TEXT")
    private String legalBasisJson;

    @Convert(converter = AesCryptoConverter.class)
    @Lob
    @Column(name = "case_basis_json", columnDefinition = "TEXT")
    private String caseBasisJson;

    @Convert(converter = AesCryptoConverter.class)
    @Lob
    @Column(name = "certification_metadata_json", columnDefinition = "TEXT")
    private String certificationMetadataJson;

    @Convert(converter = AesCryptoConverter.class)
    @Lob
    @Column(name = "raw_ml_response_json", columnDefinition = "TEXT")
    private String rawMlResponseJson;

    @Column(name = "status")
    private String status;

    @Column(name = "error_message")
    private String errorMessage;
}
