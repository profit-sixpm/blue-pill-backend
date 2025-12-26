package com.sixpm.domain.report.dto.request;

import jakarta.validation.constraints.NotBlank;

public record SearchQueryRequest(
    @NotBlank(message = "검색어는 필수입니다.")
    String query
) {}
