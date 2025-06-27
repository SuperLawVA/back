package com.superlawva.global.exception;

import com.superlawva.global.response.status.ErrorStatus;
import lombok.Getter;

@Getter
public class BaseException extends RuntimeException {

    private final ErrorStatus code;
    private final String message;

    public BaseException(ErrorStatus code) {
        this.code = code;
        this.message = code.getMessage();
    }

    public BaseException(ErrorStatus code, String message) {
        this.code = code;
        this.message = message;
    }
} 