package com.superlawva.global.exception;

import com.superlawva.global.response.status.ErrorStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
 
@Getter
@AllArgsConstructor
public class BaseException extends RuntimeException {
    private final ErrorStatus errorStatus;
    
    // 기본 생성자 추가 (컴파일 오류 해결용)
    public BaseException() {
        this.errorStatus = ErrorStatus._INTERNAL_SERVER_ERROR;
    }
} 