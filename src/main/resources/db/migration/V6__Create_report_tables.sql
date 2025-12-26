-- 사용자 상세 정보 테이블 생성 (단일 테이블로 통합)

CREATE TABLE IF NOT EXISTS user_detail_info (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,

    -- 기본 정보
    age INTEGER NOT NULL,
    residence_area VARCHAR(100) NOT NULL,
    residence_period INTEGER NOT NULL,

    -- 가구 정보
    household_members INTEGER NOT NULL,
    minor_children INTEGER NOT NULL,
    is_homeless_householder BOOLEAN NOT NULL,
    is_single_parent BOOLEAN NOT NULL,
    is_married BOOLEAN NOT NULL,

    -- 재산 정보
    monthly_income INTEGER NOT NULL,
    total_assets INTEGER NOT NULL,
    car_value INTEGER NOT NULL,

    -- 청약통장 정보
    has_savings_account BOOLEAN NOT NULL,
    payment_count INTEGER,

    -- 자격 정보
    additional_qualifications TEXT,
    is_disabled BOOLEAN NOT NULL,
    is_severely_disabled BOOLEAN NOT NULL,
    is_priority_supply BOOLEAN NOT NULL,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 인덱스 생성
CREATE INDEX idx_user_detail_info_user_id ON user_detail_info(user_id);

-- 코멘트 추가
COMMENT ON TABLE user_detail_info IS '사용자 상세 정보 (분석리포트용 - 모든 정보 통합)';
COMMENT ON COLUMN user_detail_info.user_id IS '사용자 ID (고유값)';
COMMENT ON COLUMN user_detail_info.age IS '나이';
COMMENT ON COLUMN user_detail_info.residence_area IS '거주지역';
COMMENT ON COLUMN user_detail_info.residence_period IS '거주기간 (년)';
COMMENT ON COLUMN user_detail_info.household_members IS '세대원 수';
COMMENT ON COLUMN user_detail_info.minor_children IS '미성년 자녀수';
COMMENT ON COLUMN user_detail_info.monthly_income IS '월평균 소득 (만원)';
COMMENT ON COLUMN user_detail_info.total_assets IS '총 자산 가액 (만원)';
COMMENT ON COLUMN user_detail_info.car_value IS '자동차 가액 (만원)';
COMMENT ON COLUMN user_detail_info.has_savings_account IS '청약통장 보유 여부';
COMMENT ON COLUMN user_detail_info.payment_count IS '청약통장 납입 횟수';
COMMENT ON COLUMN user_detail_info.additional_qualifications IS '추가 자격 (쉼표 구분)';

