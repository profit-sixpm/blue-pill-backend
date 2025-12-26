package com.sixpm.domain.announcement.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * 청약 공고 리스트 조회 API 응답
 */
@Data
public class AnnouncementListApiResponse {
    @JsonProperty("response")
    private Response response;

    @Data
    public static class Response {
        @JsonProperty("header")
        private Header header;

        @JsonProperty("body")
        private Body body;
    }

    @Data
    public static class Header {
        @JsonProperty("resultCode")
        private String resultCode;

        @JsonProperty("resultMsg")
        private String resultMsg;
    }

    @Data
    public static class Body {
        @JsonProperty("items")
        private Items items;

        @JsonProperty("numOfRows")
        private Integer numOfRows;

        @JsonProperty("pageNo")
        private Integer pageNo;

        @JsonProperty("totalCount")
        private Integer totalCount;
    }

    @Data
    public static class Items {
        @JsonProperty("item")
        private List<AnnouncementItem> item;
    }

    @Data
    public static class AnnouncementItem {
        @JsonProperty("HOUSE_MANAGE_NO")
        private String houseManageNo;  // 주택관리번호

        @JsonProperty("PBLANC_NO")
        private String pblancNo;  // 공고번호

        @JsonProperty("HOUSE_NM")
        private String houseNm;  // 주택명

        @JsonProperty("HOUSE_SECD")
        private String houseSecd;  // 주택구분코드

        @JsonProperty("HOUSE_SECD_NM")
        private String houseSecdNm;  // 주택구분명

        @JsonProperty("SUBSCRPT_AREA_CODE")
        private String subscrptAreaCode;  // 모집지역코드

        @JsonProperty("SUBSCRPT_AREA_CODE_NM")
        private String subscrptAreaCodeNm;  // 모집지역명

        @JsonProperty("PBLANC_URL")
        private String pblancUrl;  // 공고URL

        @JsonProperty("RCEPT_BGNDE")
        private String rceptBgnde;  // 접수시작일

        @JsonProperty("RCEPT_ENDDE")
        private String rceptEndde;  // 접수종료일

        @JsonProperty("SPSPLY_RCEPT_BGNDE")
        private String spsplyRceptBgnde;  // 특별공급 접수시작일

        @JsonProperty("SPSPLY_RCEPT_ENDDE")
        private String spsplyRceptEndde;  // 특별공급 접수종료일

        @JsonProperty("GNRL_RCEPT_BGNDE")
        private String gnrlRceptBgnde;  // 일반공급 접수시작일

        @JsonProperty("GNRL_RCEPT_ENDDE")
        private String gnrlRceptEndde;  // 일반공급 접수종료일

        @JsonProperty("PRZWNER_PRESNATN_DE")
        private String przwnerPresnatnDe;  // 당첨자발표일

        @JsonProperty("CNTRCT_CNCLS_BGNDE")
        private String cntrctCnclsBgnde;  // 계약시작일

        @JsonProperty("CNTRCT_CNCLS_ENDDE")
        private String cntrctCnclsEndde;  // 계약종료일

        @JsonProperty("HSSPLY_ADRES")
        private String hssplyAdres;  // 공급위치
    }
}

