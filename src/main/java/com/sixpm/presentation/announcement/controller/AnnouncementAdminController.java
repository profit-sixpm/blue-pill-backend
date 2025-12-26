package com.sixpm.presentation.announcement.controller;

import com.sixpm.domain.announcement.dto.request.AnnouncementFetchRequest;
import com.sixpm.domain.announcement.dto.response.AnnouncementFetchResponse;
import com.sixpm.domain.announcement.service.AnnouncementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.sixpm.domain.announcement.service.AnnouncementProcessingService; // Import 추가

/**
 * 청약 공고 관리 컨트롤러 (Admin)
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/announcements")
@RequiredArgsConstructor
@Tag(name = "Admin - Announcement", description = "청약 공고 관리 API (관리자)")
public class AnnouncementAdminController {

    private final AnnouncementService announcementService;
    private final AnnouncementProcessingService processingService;

    @PostMapping("/fetch")
    @Operation(
            summary = "청약 공고 조회 및 PDF 업로드",
            description = "특정 날짜의 청약 공고를 Open API로 조회하고 PDF를 S3에 업로드합니다. " +
                    "모든 페이지의 데이터를 조회하여 처리합니다."
    )
    public ResponseEntity<AnnouncementFetchResponse> fetchAnnouncements(
            @Valid @RequestBody AnnouncementFetchRequest request) {

        log.info("Received announcement fetch request for date: {}", request.getAnnouncementDate());

        AnnouncementFetchResponse response = announcementService.fetchAndUploadAnnouncements(request);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/process")
    @Operation(
            summary = "공고 상세 처리 수동 실행",
            description = "특정 공고 ID에 대해 파싱, 정보 추출, 벡터 임베딩 작업을 비동기로 다시 실행합니다."
    )
    public ResponseEntity<String> processAnnouncement(@PathVariable Long id) {
        log.info("Manual trigger for processing announcement ID: {}", id);
        processingService.processAnnouncementAsync(id);
        return ResponseEntity.ok("Processing started asynchronously for ID: " + id);
    }
}

