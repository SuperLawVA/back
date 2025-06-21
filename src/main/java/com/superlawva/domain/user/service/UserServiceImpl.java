package com.superlawva.domain.user.service;

import com.superlawva.domain.user.dto.PasswordChangeRequestDTO;
import com.superlawva.domain.user.dto.UserRequestDTO;
import com.superlawva.domain.user.dto.UserResponseDTO;
import com.superlawva.domain.user.entity.User;
import com.superlawva.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public List<UserResponseDTO> findAll() {
        return userRepository.findAll().stream()
                .map(UserResponseDTO::from)
                .collect(Collectors.toList());
    }

    @Override
    public UserResponseDTO findById(Long id) {
        User u = userRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("회원이 없습니다."));
        return UserResponseDTO.from(u);
    }

    @Override
    public UserResponseDTO create(UserRequestDTO dto) {
        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }
        
        // 일반 회원가입 시 password 필수 검증
        if (dto.getPassword() == null || dto.getPassword().trim().isEmpty()) {
            throw new IllegalArgumentException("비밀번호는 필수입니다.");
        }
        
        User user = User.builder()
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .nickname(dto.getNickname())
                .build();
        User saved = userRepository.save(user);
        return UserResponseDTO.from(saved);
    }

    @Override
    public UserResponseDTO update(Long id, UserRequestDTO dto) {
        User u = userRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("회원이 없습니다."));
        u.changeEmail(dto.getEmail());
        //u.changeNickname(dto.getNickname());
        User saved = userRepository.save(u);
        return UserResponseDTO.from(saved);
    }

    @Override
    public void delete(Long id) {
        userRepository.deleteById(id);
    }

    @Override
    public UserResponseDTO getMyInfo(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NoSuchElementException("회원을 찾을 수 없습니다."));
        return UserResponseDTO.from(user);
    }

    @Override
    public void changePassword(String email, PasswordChangeRequestDTO dto) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NoSuchElementException("회원을 찾을 수 없습니다."));

        if (user.getPassword() == null) {
            throw new IllegalStateException("소셜 로그인을 통해 가입한 회원은 비밀번호를 변경할 수 없습니다.");
        }

        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
            throw new IllegalArgumentException("기존 비밀번호가 일치하지 않습니다.");
        }

        user.changePassword(passwordEncoder.encode(dto.getNewPassword()));
        userRepository.save(user);
    }

    @Override
    public void processOAuth2User(String registrationId, Map<String, Object> attributes) {
        String email = (String) attributes.get("email");
        Optional<User> existing = userRepository.findByEmail(email);
        if (existing.isEmpty()) {
            User u = User.builder()
                    .email(email)
                    .nickname((String) attributes.get("nickname"))
                    .kakaoId(registrationId.equals("kakao")
                            ? Long.valueOf((String) attributes.get("id"))
                            : null)
                    .build();
            userRepository.save(u);
        } else {
            User u = existing.get();
            String newNick = (String) attributes.get("nickname");
            if (newNick != null && !newNick.equals(u.getNickname())) {
                u.changeNickname(newNick);
            }
        }
    }
}
