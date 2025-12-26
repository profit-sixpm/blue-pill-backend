package com.sixpm.domain.announcement.service;

import com.sixpm.domain.announcement.dto.response.AnnouncementDetailApiResponse;
import com.sixpm.domain.announcement.dto.response.AnnouncementListApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * 청약 공고 Open API 호출 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AnnouncementApiService {

    private final WebClient webClient;

    @Value("${announcement.api.base-url:https://api.odcloud.kr/api/ApplyhomeInfoDetailSvc/v1}")
    private String baseUrl;

    @Value("${announcement.api.service-key}")
    private String serviceKey;

    /**
     * 청약 공고 리스트 조회
     *
     * @param date 공고일자 (YYYYMMDD)
     * @param page 페이지 번호
     * @param perPage 페이지당 개수
     * @return 청약 공고 리스트
     */
    public AnnouncementListApiResponse getAnnouncementList(String date, int page, int perPage) {
        log.info("Fetching announcement list for date: {}, page: {}, perPage: {}", date, page, perPage);

        try {
            AnnouncementListApiResponse response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .scheme("https")
                            .host("api.odcloud.kr")
                            .path("/api/ApplyhomeInfoDetailSvc/v1/getAPTLttotPblancDetail")
                            .queryParam("page", page)
                            .queryParam("perPage", perPage)
                            .queryParam("serviceKey", serviceKey)
                            .queryParam("RCRIT_PBLANC_DE", date)
                            .build())
                    .retrieve()
                    .bodyToMono(AnnouncementListApiResponse.class)
                    .block();

            log.info("Successfully fetched announcement list. Total count: {}",
                    response != null && response.getResponse() != null && response.getResponse().getBody() != null
                            ? response.getResponse().getBody().getTotalCount() : 0);

            return response;
        } catch (Exception e) {
            log.error("Error fetching announcement list for date: {}", date, e);
            throw new RuntimeException("청약 공고 리스트 조회 실패: " + e.getMessage(), e);
        }
    }

    /**
     * 청약 공고 상세 조회
     *
     * @param houseManageNo 주택관리번호
     * @param pblancNo 공고번호
     * @return 청약 공고 상세
     */
    public AnnouncementDetailApiResponse getAnnouncementDetail(String houseManageNo, String pblancNo) {
        log.info("Fetching announcement detail for houseManageNo: {}, pblancNo: {}", houseManageNo, pblancNo);

        try {
            AnnouncementDetailApiResponse response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .scheme("https")
                            .host("api.odcloud.kr")
                            .path("/api/ApplyhomeInfoDetailSvc/v1/getAPTLttotPblancMdl")
                            .queryParam("page", 1)
                            .queryParam("perPage", 100)
                            .queryParam("serviceKey", serviceKey)
                            .queryParam("HOUSE_MANAGE_NO", houseManageNo)
                            .queryParam("PBLANC_NO", pblancNo)
                            .build())
                    .retrieve()
                    .bodyToMono(AnnouncementDetailApiResponse.class)
                    .block();

            log.info("Successfully fetched announcement detail");
            return response;
        } catch (Exception e) {
            log.error("Error fetching announcement detail for houseManageNo: {}, pblancNo: {}",
                    houseManageNo, pblancNo, e);
            throw new RuntimeException("청약 공고 상세 조회 실패: " + e.getMessage(), e);
        }
    }

    /**
     * PDF 파일 다운로드
     *
     * @param pdfUrl PDF URL
     * @return PDF 파일 바이트 배열
     */
    public byte[] downloadPdf(String pdfUrl) {
        log.info("Downloading PDF from URL: {}", pdfUrl);

        try {
            byte[] pdfBytes = webClient.get()
                    .uri(pdfUrl)
                    .retrieve()
                    .bodyToMono(byte[].class)
                    .block();

            log.info("Successfully downloaded PDF. Size: {} bytes", pdfBytes != null ? pdfBytes.length : 0);
            return pdfBytes;
        } catch (Exception e) {
            log.error("Error downloading PDF from URL: {}", pdfUrl, e);
            throw new RuntimeException("PDF 다운로드 실패: " + e.getMessage(), e);
        }
    }
}

