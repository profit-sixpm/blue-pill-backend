package com.sixpm.presentation.report.controller;

import com.sixpm.domain.report.dto.request.CreateReportRequest;
import com.sixpm.domain.report.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 분석리포트 API
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
@Tag(name = "Report", description = "분석리포트 API")
public class ReportController {

    private final ReportService reportService;

    @PostMapping
    @Operation(summary = "분석리포트 생성", description = "사용자 정보를 입력받아 분석리포트를 생성합니다")
    public ResponseEntity<Void> createReport(@Valid @RequestBody CreateReportRequest request) {
        log.info("Creating analysis report: {}", request);
        reportService.createReport(request);
        return ResponseEntity.ok().build();
    }
}

