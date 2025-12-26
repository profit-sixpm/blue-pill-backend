package com.sixpm.domain.announcement.service;

import com.sixpm.domain.announcement.dto.response.AnnouncementDetailApiResponse;
import com.sixpm.domain.announcement.dto.response.AnnouncementListApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

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
     * LH 분양임대공고문 조회 (리스트)
     *
     * @param date 공고게시일 (YYYYMMDD)
     * @param page 페이지 번호
     * @param perPage 페이지당 개수 (PG_SZ)
     * @return 청약 공고 리스트
     */
    public AnnouncementListApiResponse getAnnouncementList(String date, int page, int perPage) {
        log.info("Fetching LH announcement list for date: {}, page: {}, perPage: {}", date, page, perPage);

        try {
            // LH API는 배열 응답을 반환: [{"dsSch": [...]}, {"dsList": [...], "resHeader": [...]}]
            List<AnnouncementListApiResponse> responseList = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .scheme("http")
                            .host("apis.data.go.kr")
                            .path("/B552555/lhLeaseNoticeInfo1/lhLeaseNoticeInfo1")
                            .queryParam("serviceKey", serviceKey)
                            .queryParam("PG_SZ", perPage)  // 한 페이지 결과 수
                            .queryParam("PAGE", page)       // 페이지 번호
                            .queryParam("PAN_NT_ST_DT", date)  // 공고게시일
                            .build())
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<AnnouncementListApiResponse>>() {})
                    .block();

            // 두 번째 요소가 실제 데이터 (첫 번째는 dsSch만 있음)
            AnnouncementListApiResponse response = null;
            if (responseList != null && responseList.size() > 1) {
                response = responseList.get(1);  // 두 번째 요소!
            }

            log.info("Successfully fetched LH announcement list. Total: {}",
                    response != null ? response.getTotalCount() : 0);

            return response;
        } catch (Exception e) {
            log.error("Error fetching LH announcement list for date: {}", date, e);
            throw new RuntimeException("LH 청약 공고 리스트 조회 실패: " + e.getMessage(), e);
        }
    }

    /**
     * LH 분양임대공고별 상세정보 조회
     *
     * API: /B552555/lhLeaseNoticeDtlInfo1/getLeaseNoticeDtlInfo1
     *
     * @param panId 공고ID (PAN_ID)
     * @param splInfTpCd 공급정보구분코드 (SPL_INF_TP_CD)
     * @param ccrCnntSysDsCd 고객센터연계시스템구분코드 (CCR_CNNT_SYS_DS_CD)
     * @param uppAisTpCd 상위매물유형코드 (UPP_AIS_TP_CD)
     * @return 청약 공고 상세
     */
    public AnnouncementDetailApiResponse getAnnouncementDetail(
            String panId,
            String splInfTpCd,
            String ccrCnntSysDsCd,
            String uppAisTpCd) {

        log.info("Fetching LH announcement detail for panId: {}, splInfTpCd: {}", panId, splInfTpCd);

        try {
            // LH API는 배열 응답을 반환: [{"dsSch": [...]}, {"dsAhflInfo": [...], "resHeader": [...], ...}]
            // 두 번째 요소에 실제 데이터가 있음
            List<AnnouncementDetailApiResponse> responseList = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .scheme("http")
                            .host("apis.data.go.kr")
                            .path("/B552555/lhLeaseNoticeDtlInfo1/getLeaseNoticeDtlInfo1")
                            .queryParam("serviceKey", serviceKey)
                            .queryParam("PAN_ID", panId)                      // 공고ID
                            .queryParam("SPL_INF_TP_CD", splInfTpCd)          // 공급정보구분코드
                            .queryParam("CCR_CNNT_SYS_DS_CD", ccrCnntSysDsCd) // 고객센터연계시스템구분코드
                            .queryParam("UPP_AIS_TP_CD", uppAisTpCd)          // 상위매물유형코드
                            .build())
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<AnnouncementDetailApiResponse>>() {})
                    .block();

            // 두 번째 요소가 실제 데이터
            AnnouncementDetailApiResponse response = null;
            if (responseList != null && responseList.size() > 1) {
                response = responseList.get(1);  // 두 번째 요소!
            }

            if (response != null) {
                log.info("Detail API response for {}: success={}, attachments={}",
                        panId,
                        response.isSuccess(),
                        response.getAttachmentFiles() != null ? response.getAttachmentFiles().size() : 0);
            }

            log.info("Successfully fetched LH announcement detail");
            return response;
        } catch (Exception e) {
            log.error("Error fetching LH announcement detail for panId: {}", panId, e);
            throw new RuntimeException("LH 청약 공고 상세 조회 실패: " + e.getMessage(), e);
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

