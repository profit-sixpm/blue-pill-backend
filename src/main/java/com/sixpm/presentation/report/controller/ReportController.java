package com.sixpm.presentation.report.controller;

import com.sixpm.domain.announcement.dto.response.AnnouncementListResponse;
import com.sixpm.domain.announcement.dto.response.AnnouncementListResponse.AnnouncementItem;
import com.sixpm.domain.report.dto.request.CreateReportRequest;
import com.sixpm.domain.report.dto.request.SearchQueryRequest;
import com.sixpm.domain.report.dto.response.ReportResponse;
import com.sixpm.domain.report.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

  @GetMapping
  @Operation(summary = "공고 자연어 검색 (RAG)", description = "사용자의 질문과 유사한 공고를 벡터 저장소에서 검색하여 공고 목록을 반환합니다.")
  public ResponseEntity<List<AnnouncementListResponse.AnnouncementItem>> searchAnnouncements(
      @RequestParam String query
  ) {
    log.info("Searching announcements: {}", query);
    List<AnnouncementItem> response = reportService.searchAnnouncements(query);
    return ResponseEntity.ok(response);
  }

  @PostMapping
  @Operation(summary = "분석리포트 생성", description = "사용자 정보를 입력받아 분석리포트를 생성하고 결과를 반환합니다")
  public ResponseEntity<ReportResponse> createReport(@Valid @RequestBody CreateReportRequest request) {
    log.info("Creating analysis report for announcement: {}", request.getAnnouncementId());
    ReportResponse response = reportService.createReport(request);
    return ResponseEntity.ok(response);
  }
}

