-- 청약공고 테이블 생성
CREATE TABLE announcements (
    id BIGSERIAL PRIMARY KEY,

    -- 기본 식별 정보
    house_manage_no VARCHAR(50) NOT NULL,  -- 주택관리번호
    pblanc_no VARCHAR(50) NOT NULL,        -- 공고번호
    model_no VARCHAR(50),                   -- 모델번호 (상세조회에만 있음)

    -- 주택 기본 정보
    house_nm VARCHAR(255) NOT NULL,         -- 주택명
    house_secd VARCHAR(10),                 -- 주택구분코드
    house_secd_nm VARCHAR(100),             -- 주택구분명
    rent_secd VARCHAR(10),                  -- 분양구분코드 (상세조회에만 있음)
    rent_secd_nm VARCHAR(100),              -- 분양구분명 (상세조회에만 있음)

    -- 모집지역 정보
    subscrpt_area_code VARCHAR(10),         -- 모집지역코드
    subscrpt_area_code_nm VARCHAR(100),     -- 모집지역명

    -- 공급위치 정보
    hssply_zip VARCHAR(10),                 -- 공급위치 우편번호
    hssply_adres TEXT,                      -- 공급위치

    -- 공급규모
    tot_suply_hshldco VARCHAR(50),          -- 공급규모(세대수)

    -- 일정 정보
    rcrit_pblanc_de VARCHAR(8),             -- 모집공고일 (YYYYMMDD)
    rcept_bgnde VARCHAR(8),                 -- 접수시작일 (YYYYMMDD)
    rcept_endde VARCHAR(8),                 -- 접수종료일 (YYYYMMDD)
    spsply_rcept_bgnde VARCHAR(8),          -- 특별공급 접수시작일 (YYYYMMDD)
    spsply_rcept_endde VARCHAR(8),          -- 특별공급 접수종료일 (YYYYMMDD)
    gnrl_rcept_bgnde VARCHAR(8),            -- 일반공급 접수시작일 (YYYYMMDD)
    gnrl_rcept_endde VARCHAR(8),            -- 일반공급 접수종료일 (YYYYMMDD)
    przwner_presnatn_de VARCHAR(8),         -- 당첨자발표일 (YYYYMMDD)
    cntrct_cncls_bgnde VARCHAR(8),          -- 계약시작일 (YYYYMMDD)
    cntrct_cncls_endde VARCHAR(8),          -- 계약종료일 (YYYYMMDD)
    brhc_mvin_xpc_ym VARCHAR(6),            -- 입주예정월 (YYYYMM)

    -- 규제 정보
    speclt_rdn_earth_at VARCHAR(1),         -- 투기과열지구여부 (Y/N)
    mdat_trget_area_secd VARCHAR(1),        -- 조정대상지역여부 (Y/N)
    parcprc_uls_at VARCHAR(1),              -- 분양가상한제여부 (Y/N)
    imprmn_bsns_at VARCHAR(1),              -- 정비사업여부 (Y/N)
    public_house_earth_at VARCHAR(1),       -- 공공택지여부 (Y/N)
    lrscl_bldlnd_at VARCHAR(1),             -- 대규모택지개발지구여부 (Y/N)
    npln_prvopr_public_house_at VARCHAR(1), -- 수도권내민영공공택지여부 (Y/N)

    -- URL 정보
    pblanc_url TEXT,                        -- 공고URL

    -- PDF 파일 정보
    pdf_file_url TEXT,                      -- S3에 업로드된 PDF 파일 URL

    -- 메타 정보
    fetch_date VARCHAR(8) NOT NULL,         -- 데이터 수집일 (YYYYMMDD)
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- 복합 유니크 제약조건 (주택관리번호 + 공고번호로 중복 방지)
    CONSTRAINT uk_announcement UNIQUE (house_manage_no, pblanc_no)
);

-- 인덱스 생성
CREATE INDEX idx_announcements_house_manage_no ON announcements(house_manage_no);
CREATE INDEX idx_announcements_pblanc_no ON announcements(pblanc_no);
CREATE INDEX idx_announcements_house_nm ON announcements(house_nm);
CREATE INDEX idx_announcements_subscrpt_area_code ON announcements(subscrpt_area_code);
CREATE INDEX idx_announcements_rcept_bgnde ON announcements(rcept_bgnde);
CREATE INDEX idx_announcements_rcept_endde ON announcements(rcept_endde);
CREATE INDEX idx_announcements_fetch_date ON announcements(fetch_date);
CREATE INDEX idx_announcements_created_at ON announcements(created_at);

-- 코멘트 추가
COMMENT ON TABLE announcements IS '청약공고 정보';
COMMENT ON COLUMN announcements.house_manage_no IS '주택관리번호 - 청약공고 기본 식별자';
COMMENT ON COLUMN announcements.pblanc_no IS '공고번호';
COMMENT ON COLUMN announcements.house_nm IS '주택명';
COMMENT ON COLUMN announcements.fetch_date IS '데이터 수집일 (YYYYMMDD 형식)';
COMMENT ON COLUMN announcements.pdf_file_url IS 'S3에 업로드된 PDF 파일 URL';

-- updated_at 자동 업데이트 트리거
CREATE TRIGGER update_announcements_updated_at BEFORE UPDATE
    ON announcements FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

