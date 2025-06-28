package com.superlawva.domain.ml.dto;

import lombok.Data;

@Data
public class CertificateUpdateRequest {

    // 수정 가능한 필드들
    private String title;
    private String body;
    private String strategySummary;
    private String followupStrategy;

    // 발신인/수신인 정보 수정
    private ReceiverDto receiver;
    private SenderDto sender;

    @Data
    public static class ReceiverDto {
        private String name;
        private String address;
        private String detailAddress;
    }

    @Data
    public static class SenderDto {
        private String name;
        private String address;
        private String detailAddress;
    }
}