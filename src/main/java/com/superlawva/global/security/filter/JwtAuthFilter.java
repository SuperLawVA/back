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
        
        // JWT 검사에서 제외할 경로들
        boolean shouldSkip = path.startsWith("/auth") || 
                           path.startsWith("/actuator") ||
                           path.startsWith("/v3/api-docs") ||
                           path.startsWith("/swagger-ui") ||
                           path.equals("/api/health") ||
                           path.equals("/api/v1/status") ||
                           path.equals("/api/upload/health") ||
                           path.equals("/");
        
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

                Authentication auth = new UsernamePasswordAuthenticationToken(
                        email, null, List.of(new SimpleGrantedAuthority("ROLE_USER"))
                );
                SecurityContextHolder.getContext().setAuthentication(auth);
                log.debug("JWT 인증 성공: {}", email);
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
