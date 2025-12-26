package com.sixpm.domain.announcement.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * LH 분양임대공고별 상세정보 조회 API 응답
 * API: /B552555/lhLeaseNoticeDtlInfo1/getLeaseNoticeDtlInfo1
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AnnouncementDetailApiResponse {

    @JsonProperty("dsSch")
    private List<SearchCondition> searchConditions;

    @JsonProperty("resHeader")
    private List<ResponseHeader> responseHeaders;

    // 접수처정보
    @JsonProperty("dsCtrtPlc")
    private List<ContractPlace> contractPlaces;

    // 단지정보
    @JsonProperty("dsSbd")
    private List<ComplexInfo> complexInfos;

    // 공급일정
    @JsonProperty("dsSplScdl")
    private List<SupplySchedule> supplySchedules;

    // 첨부파일정보
    @JsonProperty("dsAhflInfo")
    private List<AttachmentFile> attachmentFiles;

    // 단지별첨부파일정보
    @JsonProperty("dsSbdAhfl")
    private List<ComplexAttachment> complexAttachments;

    // 기타정보
    @JsonProperty("dsEtcInfo")
    private List<EtcInfo> etcInfos;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SearchCondition {
        @JsonProperty("UPP_AIS_TP_CD")
        private String uppAisTpCd;  // 상위매물유형코드

        @JsonProperty("AIS_TP_CD")
        private String aisTpCd;  // 매물유형코드

        @JsonProperty("SPL_INF_TP_CD")
        private String splInfTpCd;  // 공급정보구분코드

        @JsonProperty("PAN_ID")
        private String panId;  // 공고ID

        @JsonProperty("CCR_CNNT_SYS_DS_CD")
        private String ccrCnntSysDsCd;  // 고객센터연계시스템구분코드
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ResponseHeader {
        @JsonProperty("SS_CODE")
        private String ssCode;  // 결과코드 (Y/N)

        @JsonProperty("RS_DTTM")
        private String rsDttm;  // 출력일시
    }

    /**
     * 접수처정보 (분양주택, 공공임대 등)
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ContractPlace {
        @JsonProperty("CTRT_PLC_ADR")
        private String ctrtPlcAdr;  // 접수처소재지주소

        @JsonProperty("CTRT_PLC_DTL_ADR")
        private String ctrtPlcDtlAdr;  // 접수처소재지상세주소

        @JsonProperty("SIL_OFC_TLNO")
        private String silOfcTlno;  // 전화번호

        @JsonProperty("SIL_OFC_OPEN_DT")
        private String silOfcOpenDt;  // 운영기간시작일시

        @JsonProperty("SIL_OFC_BCLS_DT")
        private String silOfcBclsDt;  // 운영기간종료일시

        @JsonProperty("SIL_OFC_DT")
        private String silOfcDt;  // 운영기간

        @JsonProperty("TSK_SCD_CTS")
        private String tskScdCts;  // 일정내용

        @JsonProperty("SIL_OFC_GUD_FCTS")
        private String silOfcGudFcts;  // 접수처안내사항
    }

    /**
     * 단지정보
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ComplexInfo {
        @JsonProperty("BZDT_NM")
        private String bzdtNm;  // 단지명

        @JsonProperty("LCT_ARA_ADR")
        private String lctAraAdr;  // 단지주소

        @JsonProperty("LCT_ARA_DTL_ADR")
        private String lctAraDtlAdr;  // 단지상세주소

        @JsonProperty("MIN_MAX_RSDN_DDO_AR")
        private String minMaxRsdnDdoAr;  // 전용면적

        @JsonProperty("SUM_TOT_HSH_CNT")
        private String sumTotHshCnt;  // 총세대수

        @JsonProperty("HTN_FMLA_DS_CD_NM")
        private String htnFmlaDsCdNm;  // 난방방식

        @JsonProperty("MVIN_XPC_YM")
        private String mvinXpcYm;  // 입주예정월

        @JsonProperty("TFFC_FCL_CTS")
        private String tffcFclCts;  // 교통여건

        @JsonProperty("EDC_FCL_CTS")
        private String edcFclCts;  // 교육환경

        @JsonProperty("CVN_FCL_CTS")
        private String cvnFclCts;  // 편의시설

        @JsonProperty("IDT_FCL_CTS")
        private String idtFclCts;  // 부대시설

        @JsonProperty("SPL_INF_GUD_FCTS")
        private String splInfGudFcts;  // 공급정보 안내사항
    }

    /**
     * 공급일정
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SupplySchedule {
        @JsonProperty("HS_SBSC_ACP_TRG_CD_NM")
        private String hsSbscAcpTrgCdNm;  // 구분 (일반1순위, 일반2순위 등)

        @JsonProperty("ACP_DTTM")
        private String acpDttm;  // 신청일시

        @JsonProperty("RMK")
        private String rmk;  // 신청방법

        @JsonProperty("PZWR_ANC_DT")
        private String pzwrAncDt;  // 당첨자발표일자

        @JsonProperty("PZWR_PPR_SBM_ST_DT")
        private String pzwrPprSbmStDt;  // 당첨자서류제출기간시작일

        @JsonProperty("PZWR_PPR_SBM_ED_DT")
        private String pzwrPprSbmEdDt;  // 당첨자서류제출기간종료일

        @JsonProperty("CTRT_ST_DT")
        private String ctrtStDt;  // 계약체결기간시작일

        @JsonProperty("CTRT_ED_DT")
        private String ctrtEdDt;  // 계약체결기간종료일

        @JsonProperty("SPL_SCD_GUD_FCTS")
        private String splScdGudFcts;  // 공급일정안내사항
    }

    /**
     * 첨부파일정보
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AttachmentFile {
        @JsonProperty("SL_PAN_AHFL_DS_CD_NM")
        private String slPanAhflDsCdNm;  // 파일구분명

        @JsonProperty("CMN_AHFL_NM")
        private String cmnAhflNm;  // 첨부파일명

        @JsonProperty("AHFL_URL")
        private String ahflUrl;  // 다운로드 URL
    }

    /**
     * 단지별첨부파일정보
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ComplexAttachment {
        @JsonProperty("BZDT_NM")
        private String bzdtNm;  // 단지명

        @JsonProperty("SL_PAN_AHFL_DS_CD_NM")
        private String slPanAhflDsCdNm;  // 파일구분명

        @JsonProperty("CMN_AHFL_NM")
        private String cmnAhflNm;  // 첨부파일명

        @JsonProperty("AHFL_URL")
        private String ahflUrl;  // 다운로드 URL
    }

    /**
     * 기타정보
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class EtcInfo {
        @JsonProperty("PAN_DTL_CTS")
        private String panDtlCts;  // 공고내용

        @JsonProperty("ETC_FCTS")
        private String etcFcts;  // 기타사항
    }

    // 편의 메서드
    public boolean isSuccess() {
        if (responseHeaders == null || responseHeaders.isEmpty()) {
            return false;
        }
        return "Y".equals(responseHeaders.get(0).getSsCode());
    }

    /**
     * 첨부파일 중 PDF 파일 URL 가져오기
     */
    public String getPdfUrl() {
        if (attachmentFiles == null || attachmentFiles.isEmpty()) {
            return null;
        }

        // 공고문(PDF) 또는 공고문(hwp) 파일 찾기
        for (AttachmentFile file : attachmentFiles) {
            String fileType = file.getSlPanAhflDsCdNm();
            if (fileType != null && (fileType.contains("공고문") || fileType.contains("PDF"))) {
                return file.getAhflUrl();
            }
        }

        // 첫 번째 파일 반환
        return attachmentFiles.get(0).getAhflUrl();
    }
}


