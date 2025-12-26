package com.sixpm.domain.announcement.service;

import com.sixpm.domain.announcement.dto.request.AnnouncementFetchRequest;
import com.sixpm.domain.announcement.dto.response.AnnouncementDetailApiResponse;
import com.sixpm.domain.announcement.dto.response.AnnouncementFetchResponse;
import com.sixpm.domain.announcement.dto.response.AnnouncementListApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 청약 공고 처리 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AnnouncementService {

    private final AnnouncementApiService announcementApiService;
    private final S3Service s3Service;

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

                if (listResponse == null
                        || listResponse.getResponse() == null
                        || listResponse.getResponse().getBody() == null
                        || listResponse.getResponse().getBody().getItems() == null
                        || listResponse.getResponse().getBody().getItems().getItem() == null) {
                    log.warn("No announcements found for date: {}", request.getAnnouncementDate());
                    break;
                }

                List<AnnouncementListApiResponse.AnnouncementItem> items =
                        listResponse.getResponse().getBody().getItems().getItem();

                if (items.isEmpty()) {
                    break;
                }

                log.info("Processing page {}, items count: {}", page, items.size());

                // 2. 각 공고에 대해 상세 조회 및 PDF 다운로드/업로드
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
                        log.error("Error processing announcement: {} - {}",
                                item.getHouseManageNo(), item.getPblancNo(), e);
                        failedCount++;

                        processedList.add(AnnouncementFetchResponse.ProcessedAnnouncement.builder()
                                .houseManageNo(item.getHouseManageNo())
                                .pblancNo(item.getPblancNo())
                                .houseNm(item.getHouseNm())
                                .status("FAILED")
                                .errorMessage(e.getMessage())
                                .build());
                    }
                }

                // 다음 페이지 확인
                int totalCount = listResponse.getResponse().getBody().getTotalCount();
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
     * 개별 공고 처리
     */
    private AnnouncementFetchResponse.ProcessedAnnouncement processAnnouncement(
            AnnouncementListApiResponse.AnnouncementItem item, String date) {

        log.info("Processing announcement: {} - {}", item.getHouseManageNo(), item.getHouseNm());

        try {
            // 상세 조회
            AnnouncementDetailApiResponse detailResponse = announcementApiService
                    .getAnnouncementDetail(item.getHouseManageNo(), item.getPblancNo());

            if (detailResponse == null
                    || detailResponse.getResponse() == null
                    || detailResponse.getResponse().getBody() == null
                    || detailResponse.getResponse().getBody().getItems() == null
                    || detailResponse.getResponse().getBody().getItems().getItem() == null
                    || detailResponse.getResponse().getBody().getItems().getItem().isEmpty()) {
                throw new RuntimeException("상세 정보를 찾을 수 없습니다");
            }


            // PDF URL 확인
            String pdfUrl = item.getPblancUrl();
            if (pdfUrl == null || pdfUrl.isEmpty()) {
                log.warn("No PDF URL found for announcement: {}", item.getHouseManageNo());
                return AnnouncementFetchResponse.ProcessedAnnouncement.builder()
                        .houseManageNo(item.getHouseManageNo())
                        .pblancNo(item.getPblancNo())
                        .houseNm(item.getHouseNm())
                        .status("NO_PDF")
                        .errorMessage("PDF URL이 없습니다")
                        .build();
            }

            // PDF 다운로드
            byte[] pdfBytes = announcementApiService.downloadPdf(pdfUrl);

            // S3 업로드
            String fileName = String.format("%s_%s_%s.pdf",
                    item.getHouseManageNo(),
                    item.getPblancNo(),
                    sanitizeFileName(item.getHouseNm()));

            String s3Url = s3Service.uploadPdf(pdfBytes, date, fileName);

            return AnnouncementFetchResponse.ProcessedAnnouncement.builder()
                    .houseManageNo(item.getHouseManageNo())
                    .pblancNo(item.getPblancNo())
                    .houseNm(item.getHouseNm())
                    .s3Url(s3Url)
                    .status("SUCCESS")
                    .build();

        } catch (Exception e) {
            log.error("Failed to process announcement: {}", item.getHouseManageNo(), e);
            throw new RuntimeException("공고 처리 실패: " + e.getMessage(), e);
        }
    }

    /**
     * 파일명에 사용할 수 없는 문자 제거
     */
    private String sanitizeFileName(String fileName) {
        if (fileName == null) {
            return "unknown";
        }
        return fileName.replaceAll("[^a-zA-Z0-9가-힣_\\-]", "_");
    }
}

