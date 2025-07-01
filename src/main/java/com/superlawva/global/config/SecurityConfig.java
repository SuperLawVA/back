package com.superlawva.global.config;

import com.superlawva.domain.user.entity.User;
import com.superlawva.domain.user.repository.UserRepository;
import com.superlawva.global.security.filter.JwtAuthFilter;
import com.superlawva.global.security.filter.LogoutFilter;
import com.superlawva.global.security.util.JwtTokenProvider;
import com.superlawva.global.security.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.core.env.Environment;
import com.superlawva.global.security.service.TokenBlacklistService;
import com.superlawva.global.security.service.CustomOAuth2UserService;
import com.superlawva.global.security.handler.OAuth2LoginSuccessHandler;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.userdetails.UserDetails;
import static org.springframework.boot.autoconfigure.security.servlet.PathRequest.toStaticResources;

import java.util.Arrays;

import static org.springframework.boot.autoconfigure.security.servlet.PathRequest.toStaticResources;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final UserDetailsService userDetailsService;
    private final BCryptPasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;
    private final TokenBlacklistService tokenBlacklistService;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;
    private final Environment environment;

    @Value("${oauth2.enabled:false}")
    private boolean oauth2Enabled;

    @Bean
    public JwtAuthFilter jwtAuthFilter() {
        return new JwtAuthFilter(jwtTokenProvider, userRepository);
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);

        AuthenticationManagerBuilder builder = http.getSharedObject(AuthenticationManagerBuilder.class);
        builder.authenticationProvider(provider);
        return builder.build();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        log.info("=== SecurityFilterChain 설정 시작 ===");
        
        http
            .csrf(csrf -> csrf.disable())
            .cors(withDefaults())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .headers(headers -> headers
                .frameOptions(frameOptions -> frameOptions.sameOrigin())
            );
        
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/auth/oauth2/**",
                    "/auth/login", "/auth/signup", "/auth/reissue",
                    "/verification/email-send", "/verification/email-verify",
                    "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**",
                    "/actuator/**",
                    "/upload/ocr_for_jh",
                    "/upload/ocr_for_jh/**",
                    "/auth/social/complete"
                ).permitAll()
                .anyRequest().authenticated() // 나머지 모든 요청은 인증 필요
            );
        
        http
            .addFilterBefore(jwtAuthFilter(), UsernamePasswordAuthenticationFilter.class);

        // Spring Security의 기본 로그아웃 처리 활성화
        http.logout(logout -> logout
                .logoutUrl("/auth/logout") // 로그아웃을 처리할 URL 지정
                .addLogoutHandler((request, response, authentication) -> {
                    if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
                        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
                        String email = userDetails.getUsername();
                        userRepository.findByEmail(email).ifPresent(user -> {
                            refreshTokenService.deleteRefreshToken(user.getId());
                        });
                    }
                })
                .logoutSuccessHandler((request, response, authentication) ->
                    response.setStatus(HttpServletResponse.SC_OK)
                )
        );

        // OAuth2 기능 활성화 여부에 따라 설정 적용
        if (oauth2Enabled) {
            http.oauth2Login(oauth2 -> oauth2
                    .successHandler(oAuth2LoginSuccessHandler)
                    .userInfoEndpoint(userInfo -> userInfo
                            .userService(customOAuth2UserService)
                    )
            );
        }

        log.info("=== SecurityFilterChain 설정 완료 ===");
        return http.build();
    }
}
