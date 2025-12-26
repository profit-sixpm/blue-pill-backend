package com.sixpm.domain.announcement.service;

import com.sixpm.domain.announcement.dto.request.AnnouncementFetchRequest;
import com.sixpm.domain.announcement.dto.request.AnnouncementListRequest;
import com.sixpm.domain.announcement.dto.response.*;
import com.sixpm.domain.announcement.util.RegionCodeMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 청약 공고 처리 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AnnouncementService {

    private final AnnouncementApiService announcementApiService;
    private final com.sixpm.domain.announcement.repository.AnnouncementRepository announcementRepository;
    private final AnnouncementProcessingService announcementProcessingService;

    /**
     * 특정 날짜의 청약 공고를 조회하고 PDF를 S3에 업로드
     *
     * @param request 요청 정보
     * @return 처리 결과
     */
    public AnnouncementFetchResponse fetchAndUploadAnnouncements(AnnouncementFetchRequest request) {
        log.info("Starting announcement fetch and upload for date: {}", request.getAnnouncementDate());

        List<AnnouncementFetchResponse.ProcessedAnnouncement> processedList = new ArrayList<>();
        int processedCount = 0;
        int uploadedCount = 0;
        int failedCount = 0;

        try {
            // 1. 리스트 조회 (전체 데이터 가져오기 - 페이징 처리)
            int page = 1;
            int perPage = 100;
            boolean hasMore = true;

            while (hasMore) {
                AnnouncementListApiResponse listResponse = announcementApiService
                        .getAnnouncementList(request.getAnnouncementDate(), page, perPage);

                // LH API 응답 검증
                if (listResponse == null || !listResponse.isSuccess()) {
                    log.warn("No announcements found or API failed for date: {}", request.getAnnouncementDate());
                    break;
                }

                List<AnnouncementListApiResponse.AnnouncementItem> items = listResponse.getItems();

                if (items == null || items.isEmpty()) {
                    log.info("No more items found on page {}", page);
                    break;
                }

                log.info("Processing page {}, items count: {}", page, items.size());

                // 2. 각 공고에 대해 처리 (LH API는 DTL_URL만 제공)
                for (AnnouncementListApiResponse.AnnouncementItem item : items) {
                    try {
                        processedCount++;

                        AnnouncementFetchResponse.ProcessedAnnouncement processed =
                                processAnnouncement(item, request.getAnnouncementDate());

                        processedList.add(processed);

                        if ("SUCCESS".equals(processed.getStatus())) {
                            uploadedCount++;
                        } else {
                            failedCount++;
                        }
                    } catch (Exception e) {
                        log.error("Error processing LH announcement: {} - {}",
                                item.getPanId(), item.getPanNm(), e);
                        failedCount++;

                        processedList.add(AnnouncementFetchResponse.ProcessedAnnouncement.builder()
                                .houseManageNo(item.getPanId())
                                .pblancNo(item.getPanNm())
                                .houseNm(item.getPanNm())
                                .status("FAILED")
                                .errorMessage(e.getMessage())
                                .build());
                    }
                }

                // 다음 페이지 확인
                int totalCount = listResponse.getTotalCount();
                hasMore = (page * perPage) < totalCount;
                page++;
            }

            log.info("Completed announcement processing. Processed: {}, Uploaded: {}, Failed: {}",
                    processedCount, uploadedCount, failedCount);

            return AnnouncementFetchResponse.builder()
                    .processedCount(processedCount)
                    .uploadedCount(uploadedCount)
                    .failedCount(failedCount)
                    .announcements(processedList)
                    .build();

        } catch (Exception e) {
            log.error("Error in fetchAndUploadAnnouncements", e);
            throw new RuntimeException("청약 공고 처리 실패: " + e.getMessage(), e);
        }
    }

    /**
     * 개별 LH 공고 처리: 상세조회 → AHFL_URL 추출 → DB 저장
     */
    private AnnouncementFetchResponse.ProcessedAnnouncement processAnnouncement(
            AnnouncementListApiResponse.AnnouncementItem item, String date) {

        log.info("Processing LH announcement: {} - {}", item.getPanId(), item.getPanNm());

        try {
            // 1. 이미 저장된 공고인지 확인 (중복 방지)
            if (announcementRepository.existsByHouseManageNoAndPblancNo(item.getPanId(), item.getPanNm())) {
                log.info("Announcement already exists: {} - {}", item.getPanId(), item.getPanNm());
                return buildSuccessResponse(item, "Already exists (skipped)");
            }

            // 2. 상세조회 API 호출하여 첨부파일 정보 가져오기
            AnnouncementDetailApiResponse detailResponse = announcementApiService
                    .getAnnouncementDetail(
                            item.getPanId(),           // PAN_ID
                            item.getSplInfTpCd(),      // SPL_INF_TP_CD (공급정보구분코드)
                            item.getCcrCnntSysDsCd(),  // CCR_CNNT_SYS_DS_CD (고객센터연계시스템구분코드)
                            item.getUppAisTpCd()       // UPP_AIS_TP_CD (상위매물유형코드)
                    );

            // 3. 날짜 정보 추출 및 PDF URL
            String pdfUrl = null;

            // PAN_DT: 공고게시일 (YYYYMMDD 형식)
            String rcritPblancDe = item.getPanDt();  // "20251224"
            String rceptBgnde = item.getPanDt();     // 접수시작일 = 공고게시일
            String rceptEndde = null;                // 접수종료일

            // CLSG_DT: 공고마감일 (YYYY.MM.DD 형식) -> 접수종료일로 사용
            String clsgDt = item.getClsgDt();  // "2026.01.07"
            if (clsgDt != null && !clsgDt.trim().isEmpty()) {
                rceptEndde = clsgDt.replaceAll("[.\\-\\s]", "");  // "2026.01.07" -> "20260107"
            }

            // 상세조회: PDF URL만 가져오기
            if (detailResponse != null && detailResponse.isSuccess()) {
                pdfUrl = detailResponse.getPdfUrl();
                if (pdfUrl != null && !pdfUrl.isEmpty()) {
                    log.info("PDF URL found for {}: {}", item.getPanId(), pdfUrl);
                } else {
                    log.warn("No PDF URL found for {}", item.getPanId());
                }
            } else {
                log.warn("Detail API failed for {}", item.getPanId());
            }

            log.info("Final dates for {}: rcritPblancDe={}, rceptBgnde={}, rceptEndde={}",
                    item.getPanId(), rcritPblancDe, rceptBgnde, rceptEndde);

            // 4. DB에 저장 (날짜 정보 포함)
            // 지역코드 자동 매핑 (CNP_CD가 없는 경우 지역명으로 매핑)
            String regionCode = item.getCnpCd();
            String regionName = item.getCnpCdNm();

            if ((regionCode == null || regionCode.trim().isEmpty()) && regionName != null) {
                regionCode = RegionCodeMapper.getRegionCode(regionName);
                log.info("Auto-mapped region code for {}: {} -> {}",
                        item.getPanId(), regionName, regionCode);
            }

            com.sixpm.domain.announcement.entity.Announcement announcement = com.sixpm.domain.announcement.entity.Announcement.builder()
                    .houseManageNo(item.getPanId())           // 공고ID
                    .pblancNo(item.getPanNm())                // 공고명
                    .houseNm(item.getPanNm())                 // 공고명
                    .subscrptAreaCode(regionCode)             // 지역코드 (자동 매핑)
                    .subscrptAreaCodeNm(regionName)           // 지역명
                    .rcritPblancDe(rcritPblancDe)             // 공고일자 (PAN_DT)
                    .rceptBgnde(rceptBgnde)                   // 접수시작일 (PAN_DT)
                    .rceptEndde(rceptEndde)                   // 접수종료일 (CLSG_DT)
                    .pblancUrl(item.getDtlUrl())              // 상세 URL
                    .pdfFileUrl(pdfUrl)                       // AHFL_URL (PDF 다운로드 URL)
                    .fetchDate(date)                          // 수집일자
                    .build();

            com.sixpm.domain.announcement.entity.Announcement saved = announcementRepository.save(announcement);
            log.info("Saved announcement to DB: ID={}, PAN_ID={}, PDF_URL={}",
                    saved.getId(), saved.getHouseManageNo(), saved.getPdfFileUrl());

            // 5. 비동기 상세 처리 (파싱, 자격요건 추출, 임베딩)
            if (saved.getPdfFileUrl() != null && !saved.getPdfFileUrl().isBlank()) {
                announcementProcessingService.processAnnouncementAsync(saved.getId());
            }

            return buildSuccessResponse(item, pdfUrl != null ? pdfUrl : item.getDtlUrl());

        } catch (Exception e) {
            log.error("Failed to process LH announcement: {}", item.getPanId(), e);
            return buildFailedResponse(item, e.getMessage());
        }
    }

    private AnnouncementFetchResponse.ProcessedAnnouncement buildSuccessResponse(
            AnnouncementListApiResponse.AnnouncementItem item, String url) {
        return AnnouncementFetchResponse.ProcessedAnnouncement.builder()
                .houseManageNo(item.getPanId())
                .pblancNo(item.getPanNm())
                .houseNm(item.getPanNm())
                .s3Url(url)
                .status("SUCCESS")
                .build();
    }

    private AnnouncementFetchResponse.ProcessedAnnouncement buildFailedResponse(
            AnnouncementListApiResponse.AnnouncementItem item, String errorMessage) {
        return AnnouncementFetchResponse.ProcessedAnnouncement.builder()
                .houseManageNo(item.getPanId())
                .pblancNo(item.getPanNm())
                .houseNm(item.getPanNm())
                .status("FAILED")
                .errorMessage(errorMessage)
                .build();
    }

    /**
     * 청약공고 리스트 조회 (페이징, 지역코드 필터링)
     *
     * @param request 조회 요청
     * @return 공고 리스트 응답
     */
    public AnnouncementListResponse getAnnouncementList(AnnouncementListRequest request) {
        log.info("Fetching announcement list - page: {}, size: {}, regionCode: {}",
                request.getPage(), request.getSize(), request.getRegionCode());

        // 정렬 기준 설정
        Sort sort = createSort(request.getSortBy());
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);

        // 페이징 조회
        Page<com.sixpm.domain.announcement.entity.Announcement> page;
        if (request.getRegionCode() != null && !request.getRegionCode().isEmpty()) {
            // 지역코드로 필터링
            page = announcementRepository.findBySubscrptAreaCode(request.getRegionCode(), pageable);
        } else {
            // 전체 조회
            page = announcementRepository.findAll(pageable);
        }

        // Entity -> DTO 변환
        List<AnnouncementListResponse.AnnouncementItem> items = page.getContent().stream()
                .map(this::convertToAnnouncementItem)
                .collect(Collectors.toList());

        // 페이지 정보 생성
        AnnouncementListResponse.PageInfo pageInfo = AnnouncementListResponse.PageInfo.builder()
                .currentPage(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .build();

        log.info("Found {} announcements (total: {})", items.size(), page.getTotalElements());

        return AnnouncementListResponse.builder()
                .announcements(items)
                .pageInfo(pageInfo)
                .build();
    }

    /**
     * Entity를 DTO로 변환
     */
    private AnnouncementListResponse.AnnouncementItem convertToAnnouncementItem(
            com.sixpm.domain.announcement.entity.Announcement announcement) {

        return AnnouncementListResponse.AnnouncementItem.builder()
                .id(announcement.getId())
                .announcementName(announcement.getHouseNm())
                .announcementDate(announcement.getRcritPblancDe())  // 모집공고일
                .receptionStartDate(announcement.getRceptBgnde())  // 접수 시작일
                .receptionEndDate(announcement.getRceptEndde())    // 접수 종료일
                .receptionStatus(determineReceptionStatus(announcement))  // 접수 상태
                .regionCode(announcement.getSubscrptAreaCode())
                .regionName(announcement.getSubscrptAreaCodeNm())
                .pdfUrl(announcement.getPdfFileUrl())
                .createdAt(announcement.getCreatedAt())
                .build();
    }

    /**
     * 접수 상태 판단
     */
    private String determineReceptionStatus(com.sixpm.domain.announcement.entity.Announcement announcement) {
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        String startDate = announcement.getRceptBgnde();
        String endDate = announcement.getRceptEndde();

        // 날짜가 없는 경우
        if (startDate == null || endDate == null) {
            return "공고중";
        }

        // 접수 시작 전
        if (today.compareTo(startDate) < 0) {
            return "공고중";
        }

        // 접수 기간 중
        if (today.compareTo(startDate) >= 0 && today.compareTo(endDate) <= 0) {
            return "접수중";
        }

        // 접수 마감
        return "접수마감";
    }

    /**
     * 정렬 기준 생성
     */
    private Sort createSort(String sortBy) {
        if ("RECEPTION".equalsIgnoreCase(sortBy)) {
            // 접수일순 (접수 시작일 기준)
            return Sort.by(Sort.Direction.DESC, "rceptBgnde")
                    .and(Sort.by(Sort.Direction.DESC, "createdAt"));
        }
        // 기본: 최신순 (생성일 기준)
        return Sort.by(Sort.Direction.DESC, "createdAt");
    }
}

