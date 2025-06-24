package com.superlawva.global.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.superlawva.global.security.service.RefreshTokenService;
import com.superlawva.global.security.util.JwtTokenProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class LogoutFilter extends GenericFilterBean {

    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) 
            throws IOException, ServletException {
        
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        String requestURI = request.getRequestURI();
        if (!requestURI.equals("/auth/logout") || !request.getMethod().equalsIgnoreCase("POST")) {
            filterChain.doFilter(request, response);
            return;
        }

        // JWT 토큰 추출
        String token = jwtTokenProvider.resolveToken(request);
        
        if (token == null) {
            sendErrorResponse(response, HttpStatus.BAD_REQUEST, "토큰이 없습니다.");
            return;
        }

        try {
            // 액세스 토큰 검증
            if (jwtTokenProvider.validateToken(token)) {
                // 토큰에서 사용자 ID 추출
                Long userId = jwtTokenProvider.getUserId(token);
                
                // Redis에서 해당 사용자의 리프레시 토큰 삭제
                refreshTokenService.deleteRefreshToken(userId);
                
                sendSuccessResponse(response, "로그아웃이 성공적으로 처리되었습니다.");
                log.info("사용자 로그아웃 완료: userId={}", userId);
            } else {
                sendErrorResponse(response, HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다.");
            }
        } catch (Exception e) {
            log.error("로그아웃 처리 중 오류 발생: {}", e.getMessage());
            sendErrorResponse(response, HttpStatus.INTERNAL_SERVER_ERROR, "로그아웃 처리 중 오류가 발생했습니다.");
        }
    }

    private void sendSuccessResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpStatus.OK.value());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        Map<String, Object> result = Map.of(
            "isSuccess", true,
            "code", "200",
            "message", message,
            "result", null
        );
        
        response.getWriter().write(objectMapper.writeValueAsString(result));
    }

    private void sendErrorResponse(HttpServletResponse response, HttpStatus status, String message) throws IOException {
        response.setStatus(status.value());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        Map<String, Object> result = Map.of(
            "isSuccess", false,
            "code", String.valueOf(status.value()),
            "message", message,
            "result", null
        );
        
        response.getWriter().write(objectMapper.writeValueAsString(result));
    }
} 