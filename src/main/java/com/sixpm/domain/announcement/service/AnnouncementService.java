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
     * 개별 LH 공고 처리: 상세조회 → PDF 다운로드 → DB 저장
     * LH API 상세조회를 통해 첨부파일(PDF) URL을 가져옴
     */
    private AnnouncementFetchResponse.ProcessedAnnouncement processAnnouncement(
            AnnouncementListApiResponse.AnnouncementItem item, String date) {

        log.info("Processing LH announcement: {} - {}", item.getPanId(), item.getPanNm());

        try {
            // 1. 상세조회 API 호출하여 첨부파일 정보 가져오기
            AnnouncementDetailApiResponse detailResponse = announcementApiService
                    .getAnnouncementDetail(
                            item.getPanId(),           // PAN_ID
                            item.getSplInfTpCd(),      // SPL_INF_TP_CD (공급정보구분코드)
                            "02",                       // CCR_CNNT_SYS_DS_CD (고객센터연계시스템구분코드)
                            item.getUppAisTpCd()       // UPP_AIS_TP_CD (상위매물유형코드)
                    );

            if (detailResponse == null || !detailResponse.isSuccess()) {
                log.warn("LH detail API failed for PAN_ID: {}", item.getPanId());
                return buildFailedResponse(item, "상세 정보 조회 실패");
            }

            // 2. PDF URL 추출
            String pdfUrl = detailResponse.getPdfUrl();

            if (pdfUrl == null || pdfUrl.isEmpty()) {
                log.warn("No PDF URL found for LH announcement: {}", item.getPanId());
                // PDF가 없어도 DTL_URL은 저장
                return buildSuccessResponse(item, item.getDtlUrl());
            }

            // 3. TODO: PDF 다운로드 및 S3 업로드 (추후 구현)
            // byte[] pdfBytes = announcementApiService.downloadPdf(pdfUrl);
            // String s3Url = s3Service.uploadPdf(pdfBytes, date, fileName);

            // 현재는 PDF URL만 저장
            log.info("LH announcement processed - PDF URL: {}", pdfUrl);

            return buildSuccessResponse(item, pdfUrl);

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
}

