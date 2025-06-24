package com.superlawva.global.security.converter;

import com.superlawva.global.security.util.AESUtil;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.extern.slf4j.Slf4j;

@Converter
@Slf4j
public class AesCryptoConverter implements AttributeConverter<String, String> {

    @Override
    public String convertToDatabaseColumn(String attribute) {
        // AES 암호화 임시 비활성화 - 값 그대로 저장
        return attribute;

        /*
        if (attribute == null) {
            return null;
        }
        try {
            return AESUtil.encrypt(attribute);
        } catch (Exception e) {
            log.error("AES 암호화 실패: {}", e.getMessage());
            return attribute; // 암호화 실패 시 원본 반환
        }
        */
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        // AES 복호화 임시 비활성화 - 값 그대로 반환
        return dbData;

        /*
        if (dbData == null) {
            return null;
        }
        try {
            return AESUtil.decrypt(dbData);
        } catch (Exception e) {
            log.error("AES 복호화 실패: {}", e.getMessage());
            return dbData; // 복호화 실패 시 원본 반환
        }
        */
    }
} 