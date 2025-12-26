package com.sixpm.domain.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "로그인 요청")
public class LoginRequest {

    @NotBlank(message = "사용자 아이디는 필수입니다")
    @Schema(description = "사용자 아이디", example = "testuser", required = true)
    private String username;

    @NotBlank(message = "비밀번호는 필수입니다")
    @Schema(description = "비밀번호", example = "password123", required = true)
    private String password;
}

