package com.sixpm.domain.report.dto.response;

public record DetailItem(
    String category,      // 소득, 자산, 거주지 등
    boolean passed,       // 충족 여부
    String userValue,     // 실제 값
    String criteriaValue, // 기준 값
    String message        // 상세 사유
) {}
