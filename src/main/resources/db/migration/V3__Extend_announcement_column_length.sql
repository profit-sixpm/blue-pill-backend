-- 공고명(pblanc_no)과 주택관리번호(house_manage_no) 길이 확장
-- LH API 응답에서 긴 공고명이 들어올 수 있어 varchar(50) -> varchar(200)으로 확장

ALTER TABLE announcements
    ALTER COLUMN pblanc_no TYPE VARCHAR(200);

ALTER TABLE announcements
    ALTER COLUMN house_manage_no TYPE VARCHAR(100);

