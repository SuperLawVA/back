package com.superlawva.global.exception.handler;

import com.superlawva.global.exception.BaseException;
import com.superlawva.global.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BaseException.class)
    protected ResponseEntity<Object> handleBaseException(BaseException e) {
        log.error("BaseException occurred: ", e);
        ApiResponse<Object> responseBody = ApiResponse.onFailure(
                e.getErrorStatus().getCode(),
                e.getErrorStatus().getMessage(),
                null
        );
        return new ResponseEntity<>(responseBody, e.getErrorStatus().getHttpStatus());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        log.error("Validation error: ", e);
        String errorMessage = e.getBindingResult().getFieldErrors().get(0).getDefaultMessage();
        ApiResponse<Object> responseBody = ApiResponse.onFailure("400", errorMessage, null);
        return new ResponseEntity<>(responseBody, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(BindException.class)
    protected ResponseEntity<Object> handleBindException(BindException e) {
        log.error("Bind error: ", e);
        String errorMessage = e.getBindingResult().getFieldErrors().get(0).getDefaultMessage();
        ApiResponse<Object> responseBody = ApiResponse.onFailure("400", errorMessage, null);
        return new ResponseEntity<>(responseBody, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DataAccessException.class)
    protected ResponseEntity<Object> handleDataAccessException(DataAccessException e) {
        log.error("Database error: ", e);
        ApiResponse<Object> responseBody = ApiResponse.onFailure("500", "데이터베이스 오류가 발생했습니다.", null);
        return new ResponseEntity<>(responseBody, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(RuntimeException.class)
    protected ResponseEntity<Object> handleRuntimeException(RuntimeException e) {
        log.error("Runtime error: ", e);
        ApiResponse<Object> responseBody = ApiResponse.onFailure("500", "서버 내부 오류가 발생했습니다: " + e.getMessage(), null);
        return new ResponseEntity<>(responseBody, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(Exception.class)
    protected ResponseEntity<Object> handleGeneralException(Exception e) {
        log.error("Unexpected error: ", e);
        ApiResponse<Object> responseBody = ApiResponse.onFailure("500", "예상치 못한 오류가 발생했습니다.", null);
        return new ResponseEntity<>(responseBody, HttpStatus.INTERNAL_SERVER_ERROR);
    }
} 