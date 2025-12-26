#!/bin/bash

# 청약공고 API 테스트 스크립트

set -e

# 색상 정의
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# 기본 설정
BASE_URL="${BASE_URL:-http://localhost:8080}"
API_ENDPOINT="/api/admin/announcements/fetch"

echo "======================================"
echo "청약공고 API 테스트"
echo "======================================"
echo ""
echo "Base URL: $BASE_URL"
echo "Endpoint: $API_ENDPOINT"
echo ""

# 메뉴 표시
echo "테스트 옵션을 선택하세요:"
echo "1) 오늘 날짜로 테스트"
echo "2) 어제 날짜로 테스트"
echo "3) 특정 날짜 입력"
echo "4) 최근 7일간 일괄 조회"
echo "5) 잘못된 날짜 형식 테스트 (검증 테스트)"
echo ""

read -p "선택 (1-5): " choice

case $choice in
  1)
    # 오늘 날짜
    DATE=$(date +%Y%m%d)
    echo ""
    echo -e "${GREEN}오늘 날짜로 API 호출: $DATE${NC}"
    echo ""

    curl -X POST "$BASE_URL$API_ENDPOINT" \
      -H "Content-Type: application/json" \
      -d "{\"announcementDate\": \"$DATE\"}" \
      -w "\n\nHTTP Status: %{http_code}\n" \
      | jq '.'
    ;;

  2)
    # 어제 날짜
    if [[ "$OSTYPE" == "darwin"* ]]; then
      # macOS
      DATE=$(date -v-1d +%Y%m%d)
    else
      # Linux
      DATE=$(date -d "yesterday" +%Y%m%d)
    fi

    echo ""
    echo -e "${GREEN}어제 날짜로 API 호출: $DATE${NC}"
    echo ""

    curl -X POST "$BASE_URL$API_ENDPOINT" \
      -H "Content-Type: application/json" \
      -d "{\"announcementDate\": \"$DATE\"}" \
      -w "\n\nHTTP Status: %{http_code}\n" \
      | jq '.'
    ;;

  3)
    # 특정 날짜 입력
    echo ""
    read -p "날짜를 입력하세요 (YYYYMMDD 형식, 예: 20231225): " DATE

    echo ""
    echo -e "${GREEN}입력한 날짜로 API 호출: $DATE${NC}"
    echo ""

    curl -X POST "$BASE_URL$API_ENDPOINT" \
      -H "Content-Type: application/json" \
      -d "{\"announcementDate\": \"$DATE\"}" \
      -w "\n\nHTTP Status: %{http_code}\n" \
      | jq '.'
    ;;

  4)
    # 최근 7일간 일괄 조회
    echo ""
    echo -e "${GREEN}최근 7일간 공고 일괄 조회${NC}"
    echo ""

    for i in {0..6}; do
      if [[ "$OSTYPE" == "darwin"* ]]; then
        # macOS
        DATE=$(date -v-${i}d +%Y%m%d)
      else
        # Linux
        DATE=$(date -d "$i days ago" +%Y%m%d)
      fi

      echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
      echo -e "${YELLOW}[$((i+1))/7] 날짜: $DATE${NC}"
      echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

      RESPONSE=$(curl -s -X POST "$BASE_URL$API_ENDPOINT" \
        -H "Content-Type: application/json" \
        -d "{\"announcementDate\": \"$DATE\"}" \
        -w "\n%{http_code}")

      HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
      BODY=$(echo "$RESPONSE" | sed '$d')

      if [ "$HTTP_CODE" -eq 200 ]; then
        echo "$BODY" | jq '{
          date: "'$DATE'",
          processed: .processedCount,
          uploaded: .uploadedCount,
          failed: .failedCount
        }'
        echo -e "${GREEN}✓ 성공${NC}"
      else
        echo -e "${RED}✗ 실패 (HTTP $HTTP_CODE)${NC}"
        echo "$BODY" | jq '.'
      fi

      echo ""

      # API 호출 간격 (과부하 방지)
      if [ $i -lt 6 ]; then
        sleep 2
      fi
    done

    echo "======================================"
    echo "7일간 조회 완료"
    echo "======================================"
    ;;

  5)
    # 검증 테스트
    echo ""
    echo -e "${YELLOW}유효성 검증 테스트${NC}"
    echo ""

    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo "테스트 1: 잘못된 날짜 형식 (2023-12-25)"
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    curl -X POST "$BASE_URL$API_ENDPOINT" \
      -H "Content-Type: application/json" \
      -d '{"announcementDate": "2023-12-25"}' \
      -w "\nHTTP Status: %{http_code}\n" \
      | jq '.'

    echo ""
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo "테스트 2: 빈 날짜"
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    curl -X POST "$BASE_URL$API_ENDPOINT" \
      -H "Content-Type: application/json" \
      -d '{"announcementDate": ""}' \
      -w "\nHTTP Status: %{http_code}\n" \
      | jq '.'

    echo ""
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo "테스트 3: 날짜 필드 누락"
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    curl -X POST "$BASE_URL$API_ENDPOINT" \
      -H "Content-Type: application/json" \
      -d '{}' \
      -w "\nHTTP Status: %{http_code}\n" \
      | jq '.'

    echo ""
    echo -e "${YELLOW}모든 검증 테스트 완료${NC}"
    ;;

  *)
    echo -e "${RED}잘못된 선택입니다.${NC}"
    exit 1
    ;;
esac

echo ""
echo "======================================"
echo "테스트 완료"
echo "======================================"

