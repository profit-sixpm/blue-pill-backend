package com.sixpm.presentation.auth.controller;

import com.sixpm.domain.user.dto.request.LoginRequest;
import com.sixpm.domain.user.dto.request.SignupRequest;
import com.sixpm.domain.user.dto.response.AuthResponse;
import com.sixpm.domain.user.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "인증", description = "회원가입 및 로그인 API")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    @Operation(summary = "회원가입", description = "새로운 사용자를 등록합니다")
    public ResponseEntity<AuthResponse> signup(@Valid @RequestBody SignupRequest request) {
        log.info("회원가입 요청: username={}", request.getUsername());
        AuthResponse response = authService.signup(request);
        return ResponseEntity.ok()
                .header("Authorization", "Bearer " + response.getToken())
                .body(response);
    }

    @PostMapping("/login")
    @Operation(summary = "로그인", description = "사용자 로그인 후 JWT 토큰을 발급합니다")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("로그인 요청: username={}", request.getUsername());
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok()
                .header("Authorization", "Bearer " + response.getToken())
                .body(response);
    }
}

