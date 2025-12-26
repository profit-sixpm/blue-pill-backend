# ✅ LH API JSON 파싱 에러 해결 완료!

## 🐛 문제

```
MismatchedInputException: Cannot deserialize value of type `AnnouncementListApiResponse` 
from Array value (token `JsonToken.START_ARRAY`)
```

**원인**: LH API 응답이 **배열**로 시작하는데, DTO는 **객체**로 매핑하려고 했음

## 🔍 LH API 응답 구조

```json
[
  {
    "dsSch": [...],
    "resHeader": [...],
    "dsList": [...]
  }
]
```

- 최상위가 **배열** `[...]`
- 배열의 첫 번째 요소가 실제 데이터를 담은 객체

## ✅ 해결 방법

### 1. 리스트 조회 API 수정

**Before**:
```java
.bodyToMono(AnnouncementListApiResponse.class)
```

**After**:
```java
.bodyToMono(new ParameterizedTypeReference<List<AnnouncementListApiResponse>>() {})
.block();

// 첫 번째 요소 추출
AnnouncementListApiResponse response = (responseList != null && !responseList.isEmpty())
    ? responseList.get(0)
    : null;
```

### 2. 상세 조회 API도 동일하게 수정

```java
.bodyToMono(new ParameterizedTypeReference<List<AnnouncementDetailApiResponse>>() {})
.block();

AnnouncementDetailApiResponse response = (responseList != null && !responseList.isEmpty()) 
    ? responseList.get(0) 
    : null;
```

### 3. Import 추가

```java
import org.springframework.core.ParameterizedTypeReference;
import java.util.List;
```

## 📝 변경된 파일

1. ✅ `AnnouncementApiService.java`
   - `getAnnouncementList()` 메서드 수정
   - `getAnnouncementDetail()` 메서드 수정
   - Import 추가

## 🚀 테스트

이제 API 호출이 정상 작동합니다:

```bash
curl -X POST http://localhost:8080/api/admin/announcements/fetch \
  -H "Content-Type: application/json" \
  -d '{"announcementDate": "20231225"}'
```

## 💡 핵심 포인트

1. **LH API는 배열 응답**: 모든 LH API가 `[{...}]` 형태로 응답
2. **첫 번째 요소 사용**: `responseList.get(0)`으로 실제 데이터 추출
3. **Type Safety**: `ParameterizedTypeReference`로 제네릭 타입 보존

## ⚠️ 참고사항

- 배열의 첫 번째 요소만 사용 (LH API 응답 스펙)
- null 체크 필수
- 빈 배열일 경우 null 반환

문제 해결 완료! 🎉

