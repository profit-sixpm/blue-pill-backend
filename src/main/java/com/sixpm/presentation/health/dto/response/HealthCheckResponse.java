package com.sixpm.presentation.health.dto.response;

public record HealthCheckResponse(
	String status,
	String timestamp
) {
}
