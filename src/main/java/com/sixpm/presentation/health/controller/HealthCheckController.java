package com.sixpm.presentation.health.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

import com.sixpm.presentation.health.dto.response.HealthCheckResponse;

/**
 * AWS ALB 등의 헬스체크를 위한 컨트롤러
 */
@Tag(name = "헬스체크 API", description = "서버 상태 확인")
@RestController
public class HealthCheckController {

	@Operation(summary = "헬스체크", description = "서버가 정상 작동 중인지 확인합니다.")
	@GetMapping("/health")
	public HealthCheckResponse healthCheck() {
		return new HealthCheckResponse("UP", LocalDateTime.now().toString());
	}
}
