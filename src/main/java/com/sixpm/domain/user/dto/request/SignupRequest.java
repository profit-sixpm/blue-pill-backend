package com.sixpm.domain.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "회원가입 요청")
public class SignupRequest {

    @NotBlank(message = "사용자 아이디는 필수입니다")
    @Size(min = 4, max = 50, message = "사용자 아이디는 4~50자여야 합니다")
    @Schema(description = "사용자 아이디", example = "testuser", required = true)
    private String username;

    @NotBlank(message = "비밀번호는 필수입니다")
    @Size(min = 6, max = 100, message = "비밀번호는 6자 이상이어야 합니다")
    @Schema(description = "비밀번호", example = "password123", required = true)
    private String password;
}

