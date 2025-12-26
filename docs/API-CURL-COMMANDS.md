# 청약공고 API 호출 가이드

## 🚀 기본 curl 명령어

### 1. 로컬 환경 (localhost:8080)
```bash
curl -X POST http://localhost:8080/api/admin/announcements/fetch \
  -H "Content-Type: application/json" \
  -d '{
    "announcementDate": "20231225"
  }'
```

### 2. 프로덕션 환경
```bash
curl -X POST https://your-domain.com/api/admin/announcements/fetch \
  -H "Content-Type: application/json" \
  -d '{
    "announcementDate": "20231225"
  }'
```

### 3. 출력 포맷팅 (jq 사용)
```bash
curl -X POST http://localhost:8080/api/admin/announcements/fetch \
  -H "Content-Type: application/json" \
  -d '{
    "announcementDate": "20231225"
  }' | jq '.'
```

---

## 📋 다양한 날짜로 테스트

### 오늘 날짜
```bash
TODAY=$(date +%Y%m%d)
curl -X POST http://localhost:8080/api/admin/announcements/fetch \
  -H "Content-Type: application/json" \
  -d "{
    \"announcementDate\": \"$TODAY\"
  }"
```

### 어제 날짜
```bash
YESTERDAY=$(date -v-1d +%Y%m%d)  # macOS
# YESTERDAY=$(date -d "yesterday" +%Y%m%d)  # Linux

curl -X POST http://localhost:8080/api/admin/announcements/fetch \
  -H "Content-Type: application/json" \
  -d "{
    \"announcementDate\": \"$YESTERDAY\"
  }"
```

### 특정 날짜
```bash
curl -X POST http://localhost:8080/api/admin/announcements/fetch \
  -H "Content-Type: application/json" \
  -d '{
    "announcementDate": "20231201"
  }'
```

---

## 🔍 응답 예시

### 성공 응답
```json
{
  "processedCount": 50,
  "uploadedCount": 48,
  "failedCount": 2,
  "announcements": [
    {
      "houseManageNo": "2023000001",
      "pblancNo": "20230001",
      "houseNm": "서울 강남 아파트",
      "s3Url": "https://s3.amazonaws.com/bucket/announcements/2023/12/25/...",
      "status": "SUCCESS",
      "errorMessage": null
    },
    {
      "houseManageNo": "2023000002",
      "pblancNo": "20230002",
      "houseNm": "서울 서초 오피스텔",
      "s3Url": null,
      "status": "NO_PDF",
      "errorMessage": "PDF URL이 없습니다"
    }
  ]
}
```

### 실패 응답 (400 Bad Request)
```json
{
  "timestamp": "2023-12-25T10:00:00.000+00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "날짜는 YYYYMMDD 형식이어야 합니다",
  "path": "/api/admin/announcements/fetch"
}
```

---

## 🛠️ 고급 사용법

### 1. 응답을 파일로 저장
```bash
curl -X POST http://localhost:8080/api/admin/announcements/fetch \
  -H "Content-Type: application/json" \
  -d '{
    "announcementDate": "20231225"
  }' \
  -o response.json
```

### 2. HTTP 상태 코드 확인
```bash
curl -X POST http://localhost:8080/api/admin/announcements/fetch \
  -H "Content-Type: application/json" \
  -d '{
    "announcementDate": "20231225"
  }' \
  -w "\nHTTP Status: %{http_code}\n"
```

### 3. 자세한 정보 출력 (-v verbose)
```bash
curl -v -X POST http://localhost:8080/api/admin/announcements/fetch \
  -H "Content-Type: application/json" \
  -d '{
    "announcementDate": "20231225"
  }'
```

### 4. 타임아웃 설정 (대량 데이터 처리 시)
```bash
curl -X POST http://localhost:8080/api/admin/announcements/fetch \
  -H "Content-Type: application/json" \
  -d '{
    "announcementDate": "20231225"
  }' \
  --max-time 300  # 5분 타임아웃
```

---

## 📊 응답 데이터 분석

### 성공/실패 개수만 확인
```bash
curl -s -X POST http://localhost:8080/api/admin/announcements/fetch \
  -H "Content-Type: application/json" \
  -d '{
    "announcementDate": "20231225"
  }' | jq '{processed: .processedCount, uploaded: .uploadedCount, failed: .failedCount}'
```

### 실패한 항목만 필터링
```bash
curl -s -X POST http://localhost:8080/api/admin/announcements/fetch \
  -H "Content-Type: application/json" \
  -d '{
    "announcementDate": "20231225"
  }' | jq '.announcements[] | select(.status == "FAILED")'
```

### 성공한 항목의 S3 URL 목록
```bash
curl -s -X POST http://localhost:8080/api/admin/announcements/fetch \
  -H "Content-Type: application/json" \
  -d '{
    "announcementDate": "20231225"
  }' | jq -r '.announcements[] | select(.status == "SUCCESS") | .s3Url'
```

---

## 🔐 인증이 필요한 경우 (JWT)

```bash
# JWT 토큰 발급 (로그인)
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "password"
  }' | jq -r '.token')

# 토큰으로 API 호출
curl -X POST http://localhost:8080/api/admin/announcements/fetch \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "announcementDate": "20231225"
  }'
```

---

## 🧪 유효성 검증 테스트

### 잘못된 날짜 형식 (에러)
```bash
curl -X POST http://localhost:8080/api/admin/announcements/fetch \
  -H "Content-Type: application/json" \
  -d '{
    "announcementDate": "2023-12-25"
  }'
# 응답: 400 Bad Request - "날짜는 YYYYMMDD 형식이어야 합니다"
```

### 빈 날짜 (에러)
```bash
curl -X POST http://localhost:8080/api/admin/announcements/fetch \
  -H "Content-Type: application/json" \
  -d '{
    "announcementDate": ""
  }'
# 응답: 400 Bad Request - "날짜는 필수입니다"
```

### 잘못된 JSON (에러)
```bash
curl -X POST http://localhost:8080/api/admin/announcements/fetch \
  -H "Content-Type: application/json" \
  -d 'invalid json'
# 응답: 400 Bad Request
```

---

## 📝 요청 파라미터

| 필드 | 타입 | 필수 | 형식 | 설명 | 예시 |
|------|------|------|------|------|------|
| announcementDate | String | ✅ | YYYYMMDD | 공고일자 | "20231225" |

---

## 📤 응답 필드

| 필드 | 타입 | 설명 |
|------|------|------|
| processedCount | Integer | 처리된 전체 공고 수 |
| uploadedCount | Integer | 성공적으로 업로드된 공고 수 |
| failedCount | Integer | 실패한 공고 수 |
| announcements | Array | 처리된 공고 목록 |
| ├─ houseManageNo | String | 주택관리번호 |
| ├─ pblancNo | String | 공고번호 |
| ├─ houseNm | String | 주택명 |
| ├─ s3Url | String | S3 업로드 URL (성공 시) |
| ├─ status | String | SUCCESS / FAILED / NO_PDF |
| └─ errorMessage | String | 에러 메시지 (실패 시) |

---

## 🎯 실전 예제

### 최근 7일간의 공고 수집
```bash
#!/bin/bash
for i in {0..6}; do
  DATE=$(date -v-${i}d +%Y%m%d)  # macOS
  # DATE=$(date -d "$i days ago" +%Y%m%d)  # Linux
  
  echo "Fetching announcements for $DATE..."
  
  curl -s -X POST http://localhost:8080/api/admin/announcements/fetch \
    -H "Content-Type: application/json" \
    -d "{\"announcementDate\": \"$DATE\"}" \
    | jq '{date: "'$DATE'", processed: .processedCount, uploaded: .uploadedCount, failed: .failedCount}'
  
  sleep 2  # API 호출 간격 조절
done
```

### 특정 월의 모든 공고 수집
```bash
#!/bin/bash
YEAR=2023
MONTH=12

for DAY in {01..31}; do
  DATE="${YEAR}${MONTH}${DAY}"
  
  echo "Processing $DATE..."
  
  curl -s -X POST http://localhost:8080/api/admin/announcements/fetch \
    -H "Content-Type: application/json" \
    -d "{\"announcementDate\": \"$DATE\"}" \
    > "response_${DATE}.json"
  
  sleep 1
done
```

---

## 🔍 문제 해결

### 연결 거부 (Connection refused)
```bash
# 애플리케이션이 실행 중인지 확인
curl http://localhost:8080/health

# 또는
lsof -i :8080
```

### 타임아웃
```bash
# 타임아웃 시간 증가
curl -X POST http://localhost:8080/api/admin/announcements/fetch \
  -H "Content-Type: application/json" \
  -d '{
    "announcementDate": "20231225"
  }' \
  --connect-timeout 10 \
  --max-time 600  # 10분
```

### SSL 인증서 오류 (프로덕션)
```bash
# SSL 검증 무시 (개발 환경에서만!)
curl -k -X POST https://your-domain.com/api/admin/announcements/fetch \
  -H "Content-Type: application/json" \
  -d '{
    "announcementDate": "20231225"
  }'
```

---

## 📱 다른 도구 사용

### HTTPie
```bash
http POST http://localhost:8080/api/admin/announcements/fetch \
  announcementDate="20231225"
```

### Postman (import용 curl)
```bash
curl -X POST http://localhost:8080/api/admin/announcements/fetch \
  -H "Content-Type: application/json" \
  -d '{
    "announcementDate": "20231225"
  }'
```

### wget
```bash
wget --method=POST \
  --header="Content-Type: application/json" \
  --body-data='{"announcementDate":"20231225"}' \
  -O - \
  http://localhost:8080/api/admin/announcements/fetch
```

