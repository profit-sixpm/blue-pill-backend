package com.sixpm.domain.report.dto.response;

import java.util.List;

public record ReportResponse(
    String status,        // "PASS" or "FAIL"
    int totalScore,
    List<DetailItem> details,
    AiConsulting consulting
) {}
