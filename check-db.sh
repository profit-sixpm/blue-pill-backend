#!/bin/bash

echo "🔍 PostgreSQL Database Check"
echo "=============================="

# PostgreSQL 컨테이너 접속
docker exec -it cheongyak-postgres psql -U avnadmin -d defaultdb << EOF

-- 테이블 목록
\dt

-- announcements 테이블 개수
SELECT COUNT(*) as total_announcements FROM announcements;

-- 최근 5개 공고
SELECT id, house_manage_no, house_nm, created_at
FROM announcements
ORDER BY created_at DESC
LIMIT 5;

-- Flyway 마이그레이션 히스토리
SELECT version, description, installed_on
FROM flyway_schema_history
ORDER BY installed_rank DESC;

EOF

echo ""
echo "✅ Database check completed!"

