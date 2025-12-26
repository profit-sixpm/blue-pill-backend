package com.sixpm.domain.user.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AuthResponse {
    private String token;
    private String username;
    private Long userId;

    public static AuthResponse of(String token, String username, Long userId) {
        return AuthResponse.builder()
                .token(token)
                .username(username)
                .userId(userId)
                .build();
    }
}

