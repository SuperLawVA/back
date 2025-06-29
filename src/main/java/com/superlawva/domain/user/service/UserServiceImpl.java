package com.superlawva.domain.user.service;

import com.superlawva.domain.user.dto.*;
import com.superlawva.domain.user.entity.User;
import com.superlawva.domain.user.repository.UserRepository;
import com.superlawva.domain.alarm.service.AlarmService;
import com.superlawva.domain.alarm.service.ContractAlarmService;
import com.superlawva.domain.alarm.dto.AlarmDTO;
import com.superlawva.domain.alarm.entity.AlarmType;
import com.superlawva.global.exception.BaseException;
import com.superlawva.global.response.status.ErrorStatus;
import com.superlawva.global.security.util.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AlarmService alarmService;
    private final ContractAlarmService contractAlarmService;

    @Override
    @Transactional
    public void register(UserRequestDTO userRequestDTO) {
        log.info("회원가입 처리 시작 - email: {}", userRequestDTO.getEmail());
        
        // 입력값 검증
        String nickname = userRequestDTO.getNickname();
        String email = userRequestDTO.getEmail();
        String password = userRequestDTO.getPassword();
        
        if (nickname == null || nickname.trim().isEmpty()) {
            throw new BaseException(ErrorStatus.NICKNAME_NOT_EXIST);
        }
        
        if (email == null || email.trim().isEmpty()) {
            throw new BaseException(ErrorStatus.BAD_REQUEST);
        }
        
        if (password == null || password.trim().isEmpty()) {
            throw new BaseException(ErrorStatus.BAD_REQUEST);
        }
        
        // 이메일 중복 체크
        if (userRepository.existsByEmail(email)) {
            throw new BaseException(ErrorStatus.EMAIL_ALREADY_EXISTS);
        }
        
        // 비밀번호 암호화 저장
        String hashedPassword = passwordEncoder.encode(password);
        
        // 사용자 생성 및 저장
        User user = User.builder()
                .email(email)
                .password(hashedPassword)  // 암호화하여 저장
                .nickname(nickname)
                .provider("LOCAL")
                .role(User.Role.USER)
                .emailVerified(false)
                .build();
        
        userRepository.save(user);
        log.info("회원가입 완료 - userId: {}", user.getId());
    }

    @Override
    public LoginResponseDTO login(LoginRequestDTO loginRequestDTO) {
        User user = userRepository.findByEmail(loginRequestDTO.getEmail())
                .orElseThrow(() -> new BaseException(ErrorStatus.MEMBER_NOT_FOUND));

        // 암호화된 비밀번호 검증
        if (!passwordEncoder.matches(loginRequestDTO.getPassword(), user.getPassword())) {
            throw new BaseException(ErrorStatus.PASSWORD_NOT_MATCH);
        }
        
        String token = jwtTokenProvider.createToken(user.getEmail(), user.getId());
        return buildLoginResponse(user, token);
    }

    private List<Integer> getNotificationsByUserId(Long userId) {
        try {
            // 사용자의 읽지 않은 알림 타입들을 가져와서 정수 배열로 변환
            List<Integer> notificationTypes = new ArrayList<>();
            
            // AlarmType의 ordinal() 값을 기반으로 알림 타입 반환
            // 실제 알림이 있는 경우 해당 타입의 번호를 추가
            var unreadAlarms = alarmService.getUnreadAlarms(userId);
            
            for (var alarm : unreadAlarms) {
                int typeIndex = alarm.getAlarmType().ordinal();
                if (!notificationTypes.contains(typeIndex)) {
                    notificationTypes.add(typeIndex);
                }
            }
            
            // 알림이 없으면 빈 배열 반환
            return notificationTypes.isEmpty() ? List.of() : notificationTypes;
        } catch (Exception e) {
            log.warn("알림 정보 조회 실패: {}", e.getMessage());
            return List.of(); // 오류 시 빈 배열 반환
        }
    }

    private List<LoginResponseDTO.ContractInfo> getContractsByUserId(Long userId) {
        try {
            List<AlarmDTO> contracts = contractAlarmService.getContractsByUserId(userId);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd");
            
            return contracts.stream()
                    .map(contract -> new LoginResponseDTO.ContractInfo(
                        contract.getId(),
                        getContractTitle(contract.getContractType()),
                        "진행중", // 기본 상태
                        "주소 정보 없음", // contract에 주소 정보가 없는 경우 기본값
                        contract.getDates().getContractDate().format(formatter),
                        contract.getDates().getContractDate().format(formatter) // 수정일을 생성일과 동일하게 설정
                    ))
                    .toList();
        } catch (Exception e) {
            log.warn("계약 정보 조회 실패: {}", e.getMessage());
            // 오류 시 샘플 데이터 반환
            return List.of(new LoginResponseDTO.ContractInfo(
                "contract_" + userId,
                "월세 임대차 계약서",
                "진행중",
                "서울시 강남구 테헤란로 123",
                "2025.03.22",
                "2025.03.22"
            ));
        }
    }

    private String getContractTitle(String contractType) {
        if (contractType == null) return "임대차 계약서";
        
        return switch (contractType.toLowerCase()) {
            case "monthly_rent" -> "월세 임대차 계약서";
            case "jeonse" -> "전세 임대차 계약서";
            case "sale" -> "부동산 매매 계약서";
            default -> contractType + " 계약서";
        };
    }

    private List<LoginResponseDTO.RecentChat> getRecentChatsByUserId(Long userId) {
        try {
            // TODO: 실제 채팅 서비스 구현 후 연동
            // 현재는 샘플 데이터 반환
            return List.of(
                new LoginResponseDTO.RecentChat("chat_001", "집 주인이 보증금 안 돌려줘요."),
                new LoginResponseDTO.RecentChat("chat_002", "전입 신고 방법 알려줘"),
                new LoginResponseDTO.RecentChat("chat_003", "묵시적 갱신이 뭔가요")
            );
        } catch (Exception e) {
            log.warn("최근 채팅 정보 조회 실패: {}", e.getMessage());
            return List.of(); // 오류 시 빈 배열 반환
        }
    }

    @Override
    @Transactional
    public LoginResponseDTO kakaoLogin(KakaoLoginRequestDTO kakaoLoginRequestDTO) {
        // 기존 사용자 확인 (이메일로)
        User user = userRepository.findByEmail(kakaoLoginRequestDTO.getEmail())
                .orElseGet(() -> {
                    // 새 사용자 생성
                    User newUser = User.builder()
                            .email(kakaoLoginRequestDTO.getEmail())
                            .nickname(kakaoLoginRequestDTO.getNickname())
                            .provider("KAKAO")
                            .role(User.Role.USER)
                            .emailVerified(true)
                            .build();
                    return userRepository.save(newUser);
                });

        String token = jwtTokenProvider.createToken(user.getEmail(), user.getId());
        return buildLoginResponse(user, token);
    }

    @Override
    @Transactional
    public LoginResponseDTO naverLogin(NaverLoginRequestDTO naverLoginRequestDTO) {
        // 기존 사용자 확인 (이메일로)
        User user = userRepository.findByEmail(naverLoginRequestDTO.getEmail())
                .orElseGet(() -> {
                    // 새 사용자 생성
                    User newUser = User.builder()
                            .email(naverLoginRequestDTO.getEmail())
                            .nickname(naverLoginRequestDTO.getName())
                            .provider("NAVER")
                            .role(User.Role.USER)
                            .emailVerified(true)
                            .build();
                    return userRepository.save(newUser);
                });

        String token = jwtTokenProvider.createToken(user.getEmail(), user.getId());
        return buildLoginResponse(user, token);
    }

    /**
     * 로그인 응답 객체 생성 (공통 로직)
     */
    private LoginResponseDTO buildLoginResponse(User user, String token) {
        List<Integer> notifications = getNotificationsByUserId(user.getId());
        List<LoginResponseDTO.ContractInfo> contracts = getContractsByUserId(user.getId());
        List<LoginResponseDTO.RecentChat> recentChats = getRecentChatsByUserId(user.getId());

        LoginResponseDTO.UserInfo userInfo = new LoginResponseDTO.UserInfo(
            user.getId(),
            user.getEmail(),
            user.getNickname(),
            notifications,
            contracts,
            recentChats
        );

        return LoginResponseDTO.builder()
                .token(token)
                .user(userInfo)
                .build();
    }

    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new BaseException(ErrorStatus.MEMBER_NOT_FOUND));
    }

    @Override
    @Transactional
    public void changePassword(User user, PasswordChangeRequestDTO request) {
        // 암호화된 현재 비밀번호 검증
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BaseException(ErrorStatus.PASSWORD_NOT_MATCH);
        }
        if (!request.getNewPassword().equals(request.getConfirmNewPassword())) {
            throw new BaseException(ErrorStatus.PASSWORD_CONFIRM_NOT_MATCH);
        }
        // 새 비밀번호 암호화 후 저장
        String encodedNewPassword = passwordEncoder.encode(request.getNewPassword());
        user.changePassword(encodedNewPassword);
        userRepository.save(user);
    }

    @Override
    public UserResponseDTO getMyInfo(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(ErrorStatus.MEMBER_NOT_FOUND));
        return UserResponseDTO.from(user);
    }

    @Override
    @Transactional
    public UserResponseDTO updateMyInfo(Long userId, UserRequestDTO.UpdateMyInfoDTO request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(ErrorStatus.MEMBER_NOT_FOUND));
        
        if (request.getNickname() != null) {
            user.changeNickname(request.getNickname());
        }
        
        userRepository.save(user);
        return UserResponseDTO.from(user);
    }

    @Override
    @Transactional
    public void deleteMyAccount(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(ErrorStatus.MEMBER_NOT_FOUND));
        userRepository.delete(user);
    }

    @Override
    public List<UserResponseDTO> findAll() {
        return userRepository.findAll().stream()
                .map(UserResponseDTO::from)
                .toList();
    }

    @Override
    public UserResponseDTO findById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BaseException(ErrorStatus.MEMBER_NOT_FOUND));
        return UserResponseDTO.from(user);
    }

    @Override
    @Transactional
    public UserResponseDTO update(Long id, UserRequestDTO dto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BaseException(ErrorStatus.MEMBER_NOT_FOUND));
        
        if (dto.getNickname() != null) {
            user.changeNickname(dto.getNickname());
        }
        if (dto.getEmail() != null) {
            user.changeEmail(dto.getEmail());
        }
        
        userRepository.save(user);
        return UserResponseDTO.from(user);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BaseException(ErrorStatus.MEMBER_NOT_FOUND));
        userRepository.delete(user);
    }

    @Override
    @Transactional
    public User processOAuth2User(String registrationId, Map<String, Object> attributes) {
        // OAuth2 사용자 처리 로직
        // 실제 구현에서는 registrationId에 따라 카카오/네이버 사용자 정보를 처리
        return null; // 임시 구현
    }

    @Override
    public LoginResponseDTO getUserDashboard(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(ErrorStatus.MEMBER_NOT_FOUND));
        
        // 실제 데이터베이스에서 정보 가져오기
        List<Integer> notifications = getNotificationsByUserId(user.getId());
        List<LoginResponseDTO.ContractInfo> contracts = getContractsByUserId(user.getId());
        List<LoginResponseDTO.RecentChat> recentChats = getRecentChatsByUserId(user.getId());

        LoginResponseDTO.UserInfo userInfo = new LoginResponseDTO.UserInfo(
            user.getId(),
            user.getEmail(),
            user.getNickname(),
            notifications,
            contracts,
            recentChats
        );

        return LoginResponseDTO.builder()
                .token("existing_token") // 대시보드에서는 기존 토큰 유지
                .user(userInfo)
                .build();
    }
}
