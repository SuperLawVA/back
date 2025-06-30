package com.superlawva.global.security.converter;

import com.superlawva.global.security.util.AriaUtil;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.extern.slf4j.Slf4j;

@Converter
@Slf4j
public class AriaCryptoConverter implements AttributeConverter<String, String> {

    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (attribute == null) return null;
        try {
            return AriaUtil.encrypt(attribute);
        } catch (Exception e) {
            log.error("ARIA 암호화 실패: {}", e.getMessage());
            return attribute;
        }
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        try {
            return AriaUtil.decrypt(dbData);
        } catch (Exception e) {
            log.error("ARIA 복호화 실패: {}", e.getMessage());
            return dbData;
        }
    }
} 