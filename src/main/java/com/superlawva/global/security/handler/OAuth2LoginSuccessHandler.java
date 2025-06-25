package com.superlawva.global.security.handler;

import com.superlawva.domain.user.entity.User;
import com.superlawva.domain.user.repository.UserRepository;
import com.superlawva.global.security.util.HashUtil;
import com.superlawva.global.security.util.JwtTokenProvider;
import jakarta.servlet.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import java.io.IOException;
import java.util.Map;

@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final HashUtil hashUtil;

    public OAuth2LoginSuccessHandler(JwtTokenProvider jwtTokenProvider, UserRepository userRepository, HashUtil hashUtil) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userRepository = userRepository;
        this.hashUtil = hashUtil;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest req,
                                        HttpServletResponse res,
                                        Authentication auth) throws IOException {
        OAuth2User oAuth2User = (OAuth2User) auth.getPrincipal();
        String email = (String) oAuth2User.getAttributes().get("email");
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        String token = jwtTokenProvider.createToken(email, user.getId());

        // 로그인 성공 후 리다이렉트 또는 응답 본문에 토큰 전달
        // 예: res.getWriter().write(new ObjectMapper().writeValueAsString(Map.of("token", token)));
        // 여기서는 예시로 /login/success 로 리다이렉트
        res.setHeader("Authorization", "Bearer " + token);
        res.setStatus(HttpServletResponse.SC_OK);
    }
}
