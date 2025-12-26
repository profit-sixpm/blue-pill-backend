package com.sixpm.domain.announcement.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * 청약 공고 상세 조회 API 응답
 */
@Data
public class AnnouncementDetailApiResponse {
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
        private List<AnnouncementDetailItem> item;
    }

    @Data
    public static class AnnouncementDetailItem {
        @JsonProperty("HOUSE_MANAGE_NO")
        private String houseManageNo;  // 주택관리번호

        @JsonProperty("PBLANC_NO")
        private String pblancNo;  // 공고번호

        @JsonProperty("MODEL_NO")
        private String modelNo;  // 모델번호

        @JsonProperty("HOUSE_NM")
        private String houseNm;  // 주택명

        @JsonProperty("HOUSE_SECD")
        private String houseSecd;  // 주택구분코드

        @JsonProperty("HOUSE_SECD_NM")
        private String houseSecdNm;  // 주택구분명

        @JsonProperty("RENT_SECD")
        private String rentSecd;  // 분양구분코드

        @JsonProperty("RENT_SECD_NM")
        private String rentSecdNm;  // 분양구분명

        @JsonProperty("SUBSCRPT_AREA_CODE")
        private String subscrptAreaCode;  // 모집지역코드

        @JsonProperty("SUBSCRPT_AREA_CODE_NM")
        private String subscrptAreaCodeNm;  // 모집지역명

        @JsonProperty("HSSPLY_ZIP")
        private String hssplyZip;  // 공급위치 우편번호

        @JsonProperty("HSSPLY_ADRES")
        private String hssplyAdres;  // 공급위치

        @JsonProperty("TOT_SUPLY_HSHLDCO")
        private String totSuplyHshldco;  // 공급규모(세대수)

        @JsonProperty("RCRIT_PBLANC_DE")
        private String rcritPblancDe;  // 모집공고일

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

        @JsonProperty("SPECLT_RDN_EARTH_AT")
        private String specltRdnEarthAt;  // 투기과열지구여부

        @JsonProperty("MDAT_TRGET_AREA_SECD")
        private String mdatTrgetAreaSecd;  // 조정대상지역여부

        @JsonProperty("PARCPRC_ULS_AT")
        private String parcprcUlsAt;  // 분양가상한제여부

        @JsonProperty("IMPRMN_BSNS_AT")
        private String imprmnBsnsAt;  // 정비사업여부

        @JsonProperty("PUBLIC_HOUSE_EARTH_AT")
        private String publicHouseEarthAt;  // 공공택지여부

        @JsonProperty("LRSCL_BLDLND_AT")
        private String lrsclBldlndAt;  // 대규모택지개발지구여부

        @JsonProperty("NPLN_PRVOPR_PUBLIC_HOUSE_AT")
        private String nplnPrvoprPublicHouseAt;  // 수도권내민영공공택지여부

        @JsonProperty("PBLANC_URL")
        private String pblancUrl;  // 공고URL

        @JsonProperty("BRHC_MVIN_XPC_YM")
        private String brhcMvinXpcYm;  // 입주예정월
    }
}

