package com.superlawva.global.security.handler;

import com.superlawva.domain.user.entity.User;
import com.superlawva.domain.user.repository.UserRepository;
import com.superlawva.global.security.util.HashUtil;
import com.superlawva.global.security.util.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider jwtProvider;
    private final UserRepository userRepository;
    private final HashUtil hashUtil;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();
        String registrationId = ((org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken) authentication).getAuthorizedClientRegistrationId();

        String email = extractEmail(registrationId, oauthUser);
        
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        // 토큰 생성
        String token = jwtProvider.createToken(email, user.getId());

        // 프론트엔드 URL로 리다이렉트 (환경변수로 설정 가능)
        String frontendUrl = "http://localhost:3000"; // 개발용, 운영시에는 실제 도메인
        String targetUrl = UriComponentsBuilder.fromUriString(frontendUrl + "/auth/callback")
                .queryParam("token", token)
                .build()
                .encode(StandardCharsets.UTF_8)
                .toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    private String extractEmail(String registrationId, OAuth2User oauthUser) {
        if ("kakao".equals(registrationId)) {
            Map<String, Object> kakaoAccount = oauthUser.getAttribute("kakao_account");
            if (kakaoAccount != null) {
                return (String) kakaoAccount.get("email");
            }
        } else if ("naver".equals(registrationId)) {
            Map<String, Object> response = oauthUser.getAttribute("response");
            if (response != null) {
                return (String) response.get("email");
            }
        }
        // 기본적으로 email 속성을 찾음 (다른 provider 추가 시)
        return oauthUser.getAttribute("email");
    }
}
