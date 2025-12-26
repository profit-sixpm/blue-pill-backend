package com.sixpm.presentation.announcement.controller;

import com.sixpm.domain.announcement.dto.request.AnnouncementListRequest;
import com.sixpm.domain.announcement.dto.response.AnnouncementListResponse;
import com.sixpm.domain.announcement.service.AnnouncementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 청약공고 사용자 API 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/announcements")
@RequiredArgsConstructor
@Tag(name = "청약공고 사용자 API", description = "청약공고 조회 API")
public class AnnouncementController {

    private final AnnouncementService announcementService;

    /**
     * 청약공고 리스트 조회 (페이징)
     */
    @GetMapping
    @Operation(
            summary = "청약공고 리스트 조회",
            description = "청약공고 목록을 페이징하여 조회합니다. 지역코드로 필터링 가능합니다."
    )
    public ResponseEntity<AnnouncementListResponse> getAnnouncementList(
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "페이지 크기", example = "20")
            @RequestParam(defaultValue = "20") int size,

            @Parameter(description = "지역코드 필터 (선택)", example = "11")
            @RequestParam(required = false) String regionCode,

            @Parameter(description = "정렬 기준 (LATEST: 최신순, RECEPTION: 접수일순)", example = "LATEST")
            @RequestParam(defaultValue = "LATEST") String sortBy
    ) {
        log.info("GET /api/announcements - page: {}, size: {}, regionCode: {}, sortBy: {}",
                page, size, regionCode, sortBy);

        AnnouncementListRequest request = new AnnouncementListRequest();
        request.setPage(page);
        request.setSize(size);
        request.setRegionCode(regionCode);
        request.setSortBy(sortBy);

        AnnouncementListResponse response = announcementService.getAnnouncementList(request);

        return ResponseEntity.ok(response);
    }

    /**
     * 청약공고 상세 조회
     */
    @GetMapping("/{id}")
    @Operation(
            summary = "청약공고 상세 조회",
            description = "특정 청약공고의 상세 정보를 조회합니다."
    )
    public ResponseEntity<AnnouncementListResponse.AnnouncementItem> getAnnouncementDetail(
            @Parameter(description = "공고 ID", example = "1")
            @PathVariable Long id
    ) {
        log.info("GET /api/announcements/{}", id);

        AnnouncementListResponse.AnnouncementItem announcement = announcementService.getAnnouncementDetail(id);

        return ResponseEntity.ok(announcement);
    }
}

