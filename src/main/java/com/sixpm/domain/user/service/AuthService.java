package com.sixpm.domain.user.service;

import com.sixpm.config.security.JwtTokenProvider;
import com.sixpm.domain.user.dto.request.LoginRequest;
import com.sixpm.domain.user.dto.request.SignupRequest;
import com.sixpm.domain.user.dto.response.AuthResponse;
import com.sixpm.domain.user.entity.User;
import com.sixpm.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 회원가입
     */
    @Transactional
    public AuthResponse signup(SignupRequest request) {
        // 중복 체크
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("이미 존재하는 아이디입니다");
        }

        // 사용자 생성
        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .build();

        User savedUser = userRepository.save(user);
        log.info("새로운 사용자 가입: {}", savedUser.getUsername());

        // JWT 토큰 생성
        String token = jwtTokenProvider.createToken(savedUser.getUsername(), savedUser.getId());

        return AuthResponse.of(token, savedUser.getUsername(), savedUser.getId());
    }

    /**
     * 로그인
     */
    @Transactional
    public AuthResponse login(LoginRequest request) {
        // 사용자 조회
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("아이디 또는 비밀번호가 일치하지 않습니다"));

        // 비밀번호 검증
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("아이디 또는 비밀번호가 일치하지 않습니다");
        }

        log.info("사용자 로그인: {}", user.getUsername());

        // JWT 토큰 생성
        String token = jwtTokenProvider.createToken(user.getUsername(), user.getId());

        return AuthResponse.of(token, user.getUsername(), user.getId());
    }
}

