package com.sixpm.domain.announcement.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * LH 분양임대공고문 조회 API 응답
 * API: /B552555/lhLeaseNoticeInfo1/lhLeaseNoticeInfo1
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AnnouncementListApiResponse {

    @JsonProperty("dsSch")
    private List<SearchCondition> searchConditions;

    @JsonProperty("resHeader")
    private List<ResponseHeader> responseHeaders;

    @JsonProperty("dsList")
    private List<AnnouncementItem> dataList;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SearchCondition {
        @JsonProperty("PAN_ED_DT")
        private String panEdDt;  // 공고종료일

        @JsonProperty("PAGE")
        private String page;  // 페이지

        @JsonProperty("PAN_ST_DT")
        private String panStDt;  // 공고시작일

        @JsonProperty("PG_SZ")
        private String pgSz;  // 페이지 사이즈
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ResponseHeader {
        @JsonProperty("SS_CODE")
        private String ssCode;  // 결과코드 (Y/N)

        @JsonProperty("RS_DTTM")
        private String rsDttm;  // 출력일시
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AnnouncementItem {
        @JsonProperty("RNUM")
        private String rnum;  // 순번

        @JsonProperty("UPP_AIS_TP_NM")
        private String uppAisTpNm;  // 공고유형명 (분양주택, 임대주택 등)

        @JsonProperty("UPP_AIS_TP_CD")
        private String uppAisTpCd;  // 공고유형코드

        @JsonProperty("AIS_TP_CD")
        private String aisTpCd;  // 공고세부유형코드

        @JsonProperty("AIS_TP_CD_NM")
        private String aisTpCdNm;  // 공고세부유형명 (행복주택 등)

        @JsonProperty("PAN_ID")
        private String panId;  // 공고ID (중요: 상세 조회 시 사용)

        @JsonProperty("PAN_NM")
        private String panNm;  // 공고명

        @JsonProperty("PAN_DT")
        private String panDt;  // 공고일자 (YYYYMMDD)

        @JsonProperty("PAN_NT_ST_DT")
        private String panNtStDt;  // 공고게시일 (YYYY.MM.DD)

        @JsonProperty("CNP_CD")
        private String cnpCd;  // 지역코드

        @JsonProperty("CNP_CD_NM")
        private String cnpCdNm;  // 지역명

        @JsonProperty("PAN_SS")
        private String panSs;  // 공고상태 (공고중, 접수중, 접수마감 등)

        @JsonProperty("CLSG_DT")
        private String clsgDt;  // 공고마감일 (YYYY.MM.DD)

        @JsonProperty("ALL_CNT")
        private String allCnt;  // 전체조회건수

        @JsonProperty("DTL_URL")
        private String dtlUrl;  // 공고상세 URL

        @JsonProperty("SPL_INF_TP_CD")
        private String splInfTpCd;  // 공급정보구분코드

        @JsonProperty("CCR_CNNT_SYS_DS_CD")
        private String ccrCnntSysDsCd;  // 고객센터연계시스템구분코드
    }

    // 편의 메서드
    public boolean isSuccess() {
        if (responseHeaders == null || responseHeaders.isEmpty()) {
            return false;
        }
        return "Y".equals(responseHeaders.get(0).getSsCode());
    }

    public int getTotalCount() {
        if (dataList == null || dataList.isEmpty()) {
            return 0;
        }
        try {
            return Integer.parseInt(dataList.get(0).getAllCnt());
        } catch (NumberFormatException e) {
            return dataList.size();
        }
    }

    public List<AnnouncementItem> getItems() {
        return dataList;
    }
}

