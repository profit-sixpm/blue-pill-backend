-- 자격 요건 상세 테이블 생성
CREATE TABLE announcement_criteria (
    id BIGSERIAL PRIMARY KEY,
    announcement_id BIGINT NOT NULL,
    
    -- 1. 기본 자격 요건
    residence_region VARCHAR(100),         -- 거주 제한 지역 (예: 광주광역시)
    min_age INTEGER DEFAULT 19,            -- 최소 연령 (기본 19세)
    requires_homeless BOOLEAN DEFAULT true, -- 무주택세대구성원 필수 여부
    
    -- 2. 기준 소득표 (JSONB) - { "1": 3500000, "2": 5000000 ... }
    income_benchmark JSONB,
    
    -- 3. 자산 기준 매트릭스 (JSONB) - 자녀수별 자산/자동차 한도 목록
    asset_limits JSONB,
    
    -- 4. 소득 비율 매트릭스 (JSONB) - 유형/가구원수/자녀수별 비율 목록
    income_ratios JSONB,
    
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_criteria_announcement FOREIGN KEY (announcement_id) 
        REFERENCES announcements(id) ON DELETE CASCADE
);

-- 검색 성능을 위한 인덱스
CREATE INDEX idx_criteria_announcement_id ON announcement_criteria(announcement_id);

-- JSONB 컬럼에 대한 GIN 인덱스 (필요시 내부 필드 검색 속도 향상)
CREATE INDEX idx_criteria_asset_limits ON announcement_criteria USING GIN (asset_limits);
CREATE INDEX idx_criteria_income_ratios ON announcement_criteria USING GIN (income_ratios);

-- 코멘트 추가
COMMENT ON TABLE announcement_criteria IS '공고별 상세 자격 요건 (소득/자산 기준표 포함)';
COMMENT ON COLUMN announcement_criteria.income_benchmark IS '가구원수별 기준 소득 금액표 (JSON)';
COMMENT ON COLUMN announcement_criteria.asset_limits IS '자녀수별 자산 한도 규칙 목록 (JSON)';
COMMENT ON COLUMN announcement_criteria.income_ratios IS '공급유형별 소득 비율 규칙 목록 (JSON)';

-- updated_at 자동 업데이트 트리거 적용 (기존 함수 재사용)
CREATE TRIGGER update_announcement_criteria_updated_at BEFORE UPDATE
    ON announcement_criteria FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
