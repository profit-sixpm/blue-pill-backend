package com.sixpm.domain.announcement.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "announcements")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Announcement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 기본 식별 정보
    @Column(name = "house_manage_no", nullable = false, length = 100)
    private String houseManageNo;

    @Column(name = "pblanc_no", nullable = false, length = 200)
    private String pblancNo;

    @Column(name = "model_no", length = 50)
    private String modelNo;

    // 주택 기본 정보
    @Column(name = "house_nm", nullable = false)
    private String houseNm;

    @Column(name = "house_secd", length = 10)
    private String houseSecd;

    @Column(name = "house_secd_nm", length = 100)
    private String houseSecdNm;

    @Column(name = "rent_secd", length = 10)
    private String rentSecd;

    @Column(name = "rent_secd_nm", length = 100)
    private String rentSecdNm;

    // 모집지역 정보
    @Column(name = "subscrpt_area_code", length = 10)
    private String subscrptAreaCode;

    @Column(name = "subscrpt_area_code_nm", length = 100)
    private String subscrptAreaCodeNm;

    // 공급위치 정보
    @Column(name = "hssply_zip", length = 10)
    private String hssplyZip;

    @Column(name = "hssply_adres", columnDefinition = "TEXT")
    private String hssplyAdres;

    // 공급규모
    @Column(name = "tot_suply_hshldco", length = 50)
    private String totSuplyHshldco;

    // 일정 정보
    @Column(name = "rcrit_pblanc_de", length = 8)
    private String rcritPblancDe;

    @Column(name = "rcept_bgnde", length = 8)
    private String rceptBgnde;

    @Column(name = "rcept_endde", length = 8)
    private String rceptEndde;

    @Column(name = "spsply_rcept_bgnde", length = 8)
    private String spsplyRceptBgnde;

    @Column(name = "spsply_rcept_endde", length = 8)
    private String spsplyRceptEndde;

    @Column(name = "gnrl_rcept_bgnde", length = 8)
    private String gnrlRceptBgnde;

    @Column(name = "gnrl_rcept_endde", length = 8)
    private String gnrlRceptEndde;

    @Column(name = "przwner_presnatn_de", length = 8)
    private String przwnerPresnatnDe;

    @Column(name = "cntrct_cncls_bgnde", length = 8)
    private String cntrctCnclsBgnde;

    @Column(name = "cntrct_cncls_endde", length = 8)
    private String cntrctCnclsEndde;

    @Column(name = "brhc_mvin_xpc_ym", length = 6)
    private String brhcMvinXpcYm;

    // 규제 정보
    @Column(name = "speclt_rdn_earth_at", length = 1)
    private String specltRdnEarthAt;

    @Column(name = "mdat_trget_area_secd", length = 1)
    private String mdatTrgetAreaSecd;

    @Column(name = "parcprc_uls_at", length = 1)
    private String parcprcUlsAt;

    @Column(name = "imprmn_bsns_at", length = 1)
    private String imprmnBsnsAt;

    @Column(name = "public_house_earth_at", length = 1)
    private String publicHouseEarthAt;

    @Column(name = "lrscl_bldlnd_at", length = 1)
    private String lrsclBldlndAt;

    @Column(name = "npln_prvopr_public_house_at", length = 1)
    private String nplnPrvoprPublicHouseAt;

    // URL 정보
    @Column(name = "pblanc_url", columnDefinition = "TEXT")
    private String pblancUrl;

    @Column(name = "pdf_file_url", columnDefinition = "TEXT")
    private String pdfFileUrl;

    // 메타 정보
    @Column(name = "fetch_date", nullable = false, length = 8)
    private String fetchDate;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    public Announcement(String houseManageNo, String pblancNo, String modelNo,
                       String houseNm, String houseSecd, String houseSecdNm,
                       String rentSecd, String rentSecdNm,
                       String subscrptAreaCode, String subscrptAreaCodeNm,
                       String hssplyZip, String hssplyAdres, String totSuplyHshldco,
                       String rcritPblancDe, String rceptBgnde, String rceptEndde,
                       String spsplyRceptBgnde, String spsplyRceptEndde,
                       String gnrlRceptBgnde, String gnrlRceptEndde,
                       String przwnerPresnatnDe, String cntrctCnclsBgnde, String cntrctCnclsEndde,
                       String brhcMvinXpcYm, String specltRdnEarthAt, String mdatTrgetAreaSecd,
                       String parcprcUlsAt, String imprmnBsnsAt, String publicHouseEarthAt,
                       String lrsclBldlndAt, String nplnPrvoprPublicHouseAt,
                       String pblancUrl, String pdfFileUrl, String fetchDate) {
        this.houseManageNo = houseManageNo;
        this.pblancNo = pblancNo;
        this.modelNo = modelNo;
        this.houseNm = houseNm;
        this.houseSecd = houseSecd;
        this.houseSecdNm = houseSecdNm;
        this.rentSecd = rentSecd;
        this.rentSecdNm = rentSecdNm;
        this.subscrptAreaCode = subscrptAreaCode;
        this.subscrptAreaCodeNm = subscrptAreaCodeNm;
        this.hssplyZip = hssplyZip;
        this.hssplyAdres = hssplyAdres;
        this.totSuplyHshldco = totSuplyHshldco;
        this.rcritPblancDe = rcritPblancDe;
        this.rceptBgnde = rceptBgnde;
        this.rceptEndde = rceptEndde;
        this.spsplyRceptBgnde = spsplyRceptBgnde;
        this.spsplyRceptEndde = spsplyRceptEndde;
        this.gnrlRceptBgnde = gnrlRceptBgnde;
        this.gnrlRceptEndde = gnrlRceptEndde;
        this.przwnerPresnatnDe = przwnerPresnatnDe;
        this.cntrctCnclsBgnde = cntrctCnclsBgnde;
        this.cntrctCnclsEndde = cntrctCnclsEndde;
        this.brhcMvinXpcYm = brhcMvinXpcYm;
        this.specltRdnEarthAt = specltRdnEarthAt;
        this.mdatTrgetAreaSecd = mdatTrgetAreaSecd;
        this.parcprcUlsAt = parcprcUlsAt;
        this.imprmnBsnsAt = imprmnBsnsAt;
        this.publicHouseEarthAt = publicHouseEarthAt;
        this.lrsclBldlndAt = lrsclBldlndAt;
        this.nplnPrvoprPublicHouseAt = nplnPrvoprPublicHouseAt;
        this.pblancUrl = pblancUrl;
        this.pdfFileUrl = pdfFileUrl;
        this.fetchDate = fetchDate;
    }

    public void updatePdfFileUrl(String pdfFileUrl) {
        this.pdfFileUrl = pdfFileUrl;
    }

    public void updateDetailInfo(String modelNo, String rentSecd, String rentSecdNm,
                                 String hssplyZip, String totSuplyHshldco, String rcritPblancDe,
                                 String brhcMvinXpcYm, String specltRdnEarthAt, String mdatTrgetAreaSecd,
                                 String parcprcUlsAt, String imprmnBsnsAt, String publicHouseEarthAt,
                                 String lrsclBldlndAt, String nplnPrvoprPublicHouseAt) {
        this.modelNo = modelNo;
        this.rentSecd = rentSecd;
        this.rentSecdNm = rentSecdNm;
        this.hssplyZip = hssplyZip;
        this.totSuplyHshldco = totSuplyHshldco;
        this.rcritPblancDe = rcritPblancDe;
        this.brhcMvinXpcYm = brhcMvinXpcYm;
        this.specltRdnEarthAt = specltRdnEarthAt;
        this.mdatTrgetAreaSecd = mdatTrgetAreaSecd;
        this.parcprcUlsAt = parcprcUlsAt;
        this.imprmnBsnsAt = imprmnBsnsAt;
        this.publicHouseEarthAt = publicHouseEarthAt;
        this.lrsclBldlndAt = lrsclBldlndAt;
        this.nplnPrvoprPublicHouseAt = nplnPrvoprPublicHouseAt;
    }
}

