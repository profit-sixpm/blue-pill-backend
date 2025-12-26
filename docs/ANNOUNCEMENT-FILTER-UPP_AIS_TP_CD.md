# ✅ 청약공고 리스트 조회 - UPP_AIS_TP_CD=05 필터 추가 완료

## 🎯 변경 내용

LH API 청약공고 리스트 조회 시 **공고유형코드(UPP_AIS_TP_CD)를 05번으로 필터링**하도록 수정했습니다.

### 수정 파일
- `AnnouncementApiService.java` - `getAnnouncementList()` 메서드

---

## 📋 UPP_AIS_TP_CD 코드표

| 코드 | 공고유형 |
|------|---------|
| 01 | 토지 |
| 02 | 상가 |
| 03 | 오피스텔 |
| 04 | 도시형생활주택 |
| **05** | **분양주택** ✅ |
| 06 | 임대주택 |
| 13 | 특화형주택 |

---

## 🔧 수정 내용

### Before
```java
.queryParam("serviceKey", serviceKey)
.queryParam("PG_SZ", perPage)
.queryParam("PAGE", page)
.queryParam("PAN_NT_ST_DT", date)
.build())
```

### After
```java
.queryParam("serviceKey", serviceKey)
.queryParam("PG_SZ", perPage)
.queryParam("PAGE", page)
.queryParam("PAN_NT_ST_DT", date)
.queryParam("UPP_AIS_TP_CD", "05")  // 공고유형코드 (05: 분양주택) ✅
.build())
```

---

## 🎯 효과

이제 LH API 호출 시 **분양주택 공고만 조회**됩니다:
- ✅ 분양주택 공고만 필터링
- ❌ 임대주택, 토지, 상가 등은 제외
- 🚀 불필요한 데이터 제거로 성능 향상

---

## 🧪 테스트

### API 호출 예시
```bash
# 청약공고 수집 (분양주택만)
curl -X POST http://localhost:8080/api/admin/announcements/fetch \
  -H "Content-Type: application/json" \
  -d '{"announcementDate": "20251223"}'
```

### 실제 LH API 호출
```
http://apis.data.go.kr/B552555/lhLeaseNoticeInfo1/lhLeaseNoticeInfo1?
  serviceKey=...&
  PG_SZ=100&
  PAGE=1&
  PAN_NT_ST_DT=20251223&
  UPP_AIS_TP_CD=05  ← 분양주택만!
```

---

## ✅ 완료!

이제 청약공고 조회 시 **분양주택(UPP_AIS_TP_CD=05) 공고만** 가져옵니다! 🎉

