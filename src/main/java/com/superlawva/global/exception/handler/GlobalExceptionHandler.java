package com.superlawva.global.exception.handler;

import com.superlawva.global.exception.BaseException;
import com.superlawva.global.response.ApiResponse;
import com.superlawva.global.response.status.ErrorStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        
        Map<String, String> errors = ex.getBindingResult().getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        fieldError -> fieldError.getField(),
                        fieldError -> Optional.ofNullable(fieldError.getDefaultMessage()).orElse(""),
                        (existingValue, newValue) -> existingValue + ", " + newValue,
                        LinkedHashMap::new
                ));

        ApiResponse<Object> body = ApiResponse.onFailure(
                ErrorStatus.BAD_REQUEST.getCode(),
                ErrorStatus.BAD_REQUEST.getMessage(),
                errors
        );

        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ApiResponse<Object>> handleBaseException(BaseException e) {
        ApiResponse<Object> body = ApiResponse.onFailure(e.getCode().getCode(), e.getMessage(), null);
        return new ResponseEntity<>(body, e.getCode().getHttpStatus());
    }

    @ExceptionHandler
    public ResponseEntity<ApiResponse<Object>> exception(Exception e, WebRequest request) {
        log.error("Unexpected error occurred", e);
        ApiResponse<Object> body = ApiResponse.onFailure(
                ErrorStatus.INTERNAL_SERVER_ERROR.getCode(),
                ErrorStatus.INTERNAL_SERVER_ERROR.getMessage(),
                null
        );
        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }
} 