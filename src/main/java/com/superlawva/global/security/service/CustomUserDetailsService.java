package com.superlawva.global.security.service;

import com.superlawva.domain.user.entity.User;
import com.superlawva.domain.user.repository.UserRepository;
import com.superlawva.global.security.util.HashUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepo;
    private final HashUtil hashUtil;

    @Override
    public UserDetails loadUserByUsername(String email) {
        User u = userRepo.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("사용자 없음"));
        return org.springframework.security.core.userdetails.User
                .withUsername(u.getEmail())
                .password(u.getPassword())
                .roles("USER")
                .build();
    }
}
