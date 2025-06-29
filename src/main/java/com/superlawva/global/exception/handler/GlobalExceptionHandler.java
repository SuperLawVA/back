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

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler
    public ResponseEntity<Object> validation(MethodArgumentNotValidException e, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        Map<String, String> errors = new LinkedHashMap<>();

        e.getBindingResult().getFieldErrors().forEach(fieldError -> {
            String fieldName = fieldError.getField();
            String errorMessage = Optional.ofNullable(fieldError.getDefaultMessage()).orElse("");
            errors.merge(fieldName, errorMessage, (existing, newMessage) -> existing + ", " + newMessage);
        });

        return handleExceptionInternalArgs(e, HttpHeaders.EMPTY, ErrorStatus.valueOf("_BAD_REQUEST"), request, errors);
    }

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<Object> handleBaseException(BaseException e, WebRequest request) {
        return handleExceptionInternal(e, e.getCode(), HttpHeaders.EMPTY, request);
    }

    @ExceptionHandler
    public ResponseEntity<Object> exception(Exception e, WebRequest request) {
        return handleExceptionInternal(e, ErrorStatus.INTERNAL_SERVER_ERROR, HttpHeaders.EMPTY, request);
    }

    protected ResponseEntity<Object> handleExceptionInternal(Exception e, Object body,
                                                           HttpHeaders headers, WebRequest request) {
        return handleExceptionInternal(e, body, headers, null, request);
    }

    protected ResponseEntity<Object> handleExceptionInternal(Exception e, Object body,
                                                           HttpHeaders headers, HttpStatus status,
                                                           WebRequest request) {
        return handleExceptionInternalArgs(e, headers, status, request, null);
    }

    private ResponseEntity<Object> handleExceptionInternalArgs(Exception e, HttpHeaders headers, ErrorStatus status,
                                                             WebRequest request, Map<String, String> errorArgs) {
        ApiResponse<Object> body = ApiResponse.onFailure(status.getCode(), status.getMessage(), errorArgs);
        return super.handleExceptionInternal(
                e,
                body,
                headers,
                status.getHttpStatus(),
                request
        );
    }

    private ResponseEntity<Object> handleExceptionInternalArgs(Exception e, HttpHeaders headers, HttpStatus status,
                                                             WebRequest request, Map<String, String> errorArgs) {
        ApiResponse<Object> body = ApiResponse.onFailure(status.toString(), "Bad Request", errorArgs);
        return super.handleExceptionInternal(
                e,
                body,
                headers,
                status,
                request
        );
    }
} 