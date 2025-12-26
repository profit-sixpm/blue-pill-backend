# 청약공고 조회 성능 최적화

## 개선 내역

### 1. Virtual Thread 적용
- **Java 21 Virtual Thread** 사용으로 경량 스레드 기반 병렬 처리
- `spring.threads.virtual.enabled=true` 설정 활성화
- 수천 개의 동시 API 호출 처리 가능

### 2. 병렬 처리 구현
**AnnouncementService**:
- `CompletableFuture`를 사용한 비동기 병렬 처리
- Virtual Thread Executor로 각 공고를 독립적으로 처리
- 페이지 단위로 모든 공고를 동시에 처리

**변경 전**:
```java
for (AnnouncementListApiResponse.AnnouncementItem item : items) {
    processAnnouncement(item, date);  // 순차 처리
}
```

**변경 후**:
```java
List<CompletableFuture<ProcessedAnnouncement>> futures = items.stream()
    .map(item -> CompletableFuture.supplyAsync(() -> {
        return processAnnouncement(item, date);
    }, virtualThreadExecutor))
    .collect(Collectors.toList());

CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
```

### 3. WebClient 최적화
**커넥션 풀 설정**:
- 최대 커넥션 수: 500
- 대기 큐 크기: 1000
- 커넥션 재사용으로 성능 향상

**타임아웃 설정**:
- 연결 타임아웃: 10초
- 응답 타임아웃: 30초
- Read/Write 타임아웃: 30초

**Reactor Netty 활용**:
```java
ConnectionProvider connectionProvider = ConnectionProvider.builder("custom")
    .maxConnections(500)
    .pendingAcquireMaxCount(1000)
    .pendingAcquireTimeout(Duration.ofSeconds(45))
    .maxIdleTime(Duration.ofSeconds(60))
    .maxLifeTime(Duration.ofSeconds(120))
    .build();
```

### 4. 비동기 API 호출
**AnnouncementApiService**:
- `getAnnouncementDetailAsync()` 메서드 추가
- Reactor의 `Mono`를 활용한 완전 비동기 처리
- 10초 타임아웃으로 장시간 대기 방지

## 성능 개선 효과

### 예상 성능 향상
- **100개 공고 처리 시간**:
  - 변경 전: ~100초 (순차 처리, 각 1초)
  - 변경 후: ~2-3초 (병렬 처리)
  - **약 30-50배 성능 향상**

### 리소스 효율성
- Virtual Thread 사용으로 메모리 사용량 최소화
- Platform Thread 대비 1/1000 수준의 메모리 사용
- 수만 개의 동시 요청 처리 가능

## 주의사항

### API 호출 제한
- 외부 API의 Rate Limit 고려 필요
- 필요시 속도 제한(Rate Limiting) 추가 고려

### 에러 처리
- 개별 공고 처리 실패 시 다른 공고에 영향 없음
- 각 공고별로 독립적인 에러 핸들링

### 모니터링
- 로그를 통한 처리 상태 추적
- 성공/실패 카운트 별도 집계

## 향후 개선 가능 항목

1. **Resilience4j 적용**
   - Circuit Breaker 패턴
   - Retry 정책
   - Bulkhead 패턴

2. **캐싱 전략**
   - Redis 캐시 도입
   - 중복 조회 방지

3. **배치 처리**
   - Spring Batch 적용
   - 대용량 데이터 처리

4. **메트릭 수집**
   - Micrometer 활용
   - 성능 지표 모니터링

