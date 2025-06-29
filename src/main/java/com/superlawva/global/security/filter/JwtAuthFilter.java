package com.superlawva.global.security.filter;

import com.superlawva.domain.user.repository.UserRepository;
import com.superlawva.global.security.util.JwtTokenProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Slf4j
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    public JwtAuthFilter(JwtTokenProvider jwtTokenProvider,
                         UserRepository userRepository) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userRepository = userRepository;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        log.debug("JwtAuthFilter 경로 체크: {}", path);
        
        // 인증이 필요 없는 경로만 선택적으로 제외
        boolean shouldSkip = path.equals("/auth/login") ||
                           path.equals("/auth/signup") ||
                           path.equals("/auth/social") ||
                           path.equals("/auth/kakao") ||
                           path.equals("/auth/naver") ||
                           path.startsWith("/actuator") ||
                           path.startsWith("/v3/api-docs") ||
                           path.startsWith("/swagger-ui") ||
                           path.startsWith("/api/health") ||
                           path.startsWith("/api/v1/status") ||
                           path.startsWith("/api/upload/health") ||
                           path.equals("/health") ||
                           path.equals("/") ||
                           path.startsWith("/login") ||
                           path.startsWith("/error");
        
        if (shouldSkip) {
            log.debug("JWT 필터 건너뜀: {}", path);
        }
        
        return shouldSkip;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws IOException, ServletException {

        log.debug("JWT 필터 실행: {}", request.getRequestURI());

        try {
            String token = resolveToken(request);

            if (token != null && jwtTokenProvider.validateToken(token)) {
                String email = jwtTokenProvider.getEmail(token);
                Long userId = jwtTokenProvider.getUserId(token);

                // userId를 요청 속성에 저장하여 컨트롤러나 서비스에서 손쉽게 접근 가능
                request.setAttribute("userId", userId);

                Authentication auth = new UsernamePasswordAuthenticationToken(
                        email, null, List.of(new SimpleGrantedAuthority("ROLE_USER"))
                );
                SecurityContextHolder.getContext().setAuthentication(auth);
                log.debug("JWT 인증 성공: {} (userId={})", email, userId);
            }
        } catch (Exception e) {
            log.warn("JWT 토큰 처리 중 오류 발생: {}", e.getMessage());
            // 토큰 처리 실패 시에도 요청은 계속 진행
        }

        chain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest req) {
        String bearer = req.getHeader("Authorization");
        if (bearer != null && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        return null;
    }
}
