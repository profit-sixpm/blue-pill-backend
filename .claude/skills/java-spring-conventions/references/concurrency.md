# Virtual Threads and Concurrency Guide

## Virtual Threads (Project Loom)

### What are Virtual Threads?

**Definition**:
- Lightweight threads managed by JVM
- Thousands can run concurrently
- Synchronous code without complexity
- Modern alternative to thread pools

### Benefits

**Scalability**:
- Handle more concurrent requests
- Reduce context switching overhead
- Fewer threads needed

**Simplicity**:
- Write synchronous blocking code
- No callback chains or futures
- Easier to understand and debug

**Efficiency**:
- Much lighter memory footprint
- Sleep doesn't block OS thread
- Perfect for I/O-bound operations

### Code Example - Virtual Thread Configuration

**Spring Boot Configuration**:
```properties
# application.properties
spring.threads.virtual.enabled=true
spring.jpa.properties.hibernate.jdbc.batch_size=20
spring.jpa.properties.hibernate.order_inserts=true
spring.jpa.properties.hibernate.order_updates=true
```

**DO: Blocking I/O with Virtual Threads**:
```java
/**
 * 주문 서비스.
 * Virtual thread에서 blocking I/O(DB, HTTP)를 효율적으로 처리합니다.
 */
@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final RestClient restClient;

    /**
     * 주문을 생성합니다.
     * Blocking I/O 작업들(DB 저장, HTTP 호출)이 sequential하게 처리되지만,
     * virtual thread 덕분에 수천 개의 동시 요청을 OS thread 낭비 없이 처리합니다.
     *
     * @param request 주문 생성 요청
     * @return 생성된 주문 정보
     */
    @Transactional
    public OrderDto createOrder(CreateOrderRequest request) {
        // Blocking I/O 1: Database save - virtual thread에서 효율적으로 처리
        Order order = new Order(request.userId(), request.totalPrice());
        orderRepository.save(order);

        // Blocking I/O 2: HTTP call - virtual thread에서 효율적으로 처리
        UserValidationResponse validation = restClient.get()
            .uri("/validate/user/{id}", request.userId())
            .retrieve()
            .body(UserValidationResponse.class);

        if (!validation.isValid()) {
            throw new InvalidUserException();
        }

        return new OrderDto(order.getId(), order.getTotalPrice());
    }

    /**
     * 여러 주문을 배치로 조회합니다.
     * Virtual thread 덕분에 동시성 높게 처리 가능합니다.
     *
     * @param orderIds 조회할 주문 ID 목록
     * @return 주문 DTO 목록
     */
    public List<OrderDto> getOrders(List<Long> orderIds) {
        return orderRepository.findAllById(orderIds).stream()
            .map(order -> new OrderDto(order.getId(), order.getTotalPrice()))
            .toList();
    }
}
```

**DO NOT: Pinning Virtual Threads (CPU-bound operations)**:
```java
/**
 * 피해야 할 패턴: Virtual thread가 OS thread에 pinned되어 virtual thread의 이점을 잃습니다.
 */
@Service
public class BadOrderService {
    /**
     * 문제: synchronized 블록은 virtual thread를 pinning시킵니다.
     * Virtual thread가 platform thread에 고정되어 동시성 이점을 상실합니다.
     */
    public synchronized void processOrder(Order order) {
        // Heavy CPU-bound operation blocks OS thread
        long result = expensiveCalculation(order);
        order.applyDiscount(result);
    }

    /**
     * 문제: CPU-bound 연산이 virtual thread를 blocx합니다.
     * Virtual thread는 I/O 대기용이므로 CPU-bound 작업은 virtual thread 효율을 떨어뜨립니다.
     */
    public void complexCalculation(Order order) {
        // Complex calculation pinning virtual thread
        for (int i = 0; i < 1_000_000_000; i++) {
            Math.sqrt(i);
        }
    }
}
```

**DO: Use ReentrantLock instead of synchronized**:
```java
/**
 * 개선된 패턴: ReentrantLock은 virtual thread를 pinning하지 않습니다.
 */
@Service
public class GoodOrderService {
    private final Lock lock = new ReentrantLock();
    private final OrderRepository orderRepository;

    /**
     * ReentrantLock을 사용하여 virtual thread pinning을 피합니다.
     * Virtual thread는 lock 대기 중에도 OS thread를 해제합니다.
     *
     * @param orderId 처리할 주문 ID
     */
    public void processOrder(long orderId) {
        lock.lock();
        try {
            Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException());
            order.confirm();
            // Virtual thread is not pinned during lock wait
        } finally {
            lock.unlock();
        }
    }
}
```

## Virtual Thread Configuration

### Spring Boot Setup

**Enable Virtual Threads**:
- Property: spring.threads.virtual.enabled=true
- Automatic configuration in Boot 3.2+
- Executor uses virtual thread factory

**Version Requirement**:
- Java 21+ required
- Spring Boot 3.2+
- Gradle/Maven dependencies updated

## Using Virtual Threads

### Web Requests

**Default Behavior**:
- Spring automatically uses virtual threads
- Each request on its own virtual thread
- I/O operations don't block OS thread

### Service Layer

**Pattern**:
- Services use synchronous blocking code
- RestClient calls block, but don't waste thread
- Database queries block on virtual thread

**Benefits**:
- Simple imperative code
- No async/await complexity
- Natural error handling (try/catch)

### Repository/Data Access

**Blocking I/O**:
- Database calls are blocking
- Virtual thread handles blocking well
- No need for reactive drivers

**Performance**:
- Thousands of concurrent queries possible
- Physical database connection pooling still needed
- Virtual thread doesn't replace connection pool

## Concurrency Considerations

### Thread Safety

**Shared State**:
- Avoid mutable shared state
- Use immutable objects when possible
- Synchronize if mutation necessary

**Virtual Thread Limitations**:
- Cannot use ThreadLocal extensively
- Use structured concurrency instead
- ScopedValue replaces ThreadLocal

### I/O Operations

**Blocking I/O is Efficient**:
- RestClient calls: Blocking is fine
- Database queries: Blocking is fine
- File operations: Blocking is fine

**CPU-Bound Operations**:
- Regular threads for CPU work
- Virtual threads for I/O waiting
- Don't block virtual thread with CPU work

## Structured Concurrency

### Concept

**Scope**:
- Concurrency within well-defined scope
- All tasks complete before scope exits
- Better error handling
- Resource cleanup automatic

**Alternative to Threads**:
- TaskExecutor for parallel work
- Virtual threads + structured execution
- Modern concurrency pattern

## Database Connection Pooling

### Connection Pool Configuration

**Pool Size**:
- Even with virtual threads, manage connection pool
- Connections are limited resource
- Set appropriate pool size
- Usually smaller than thread count

**HikariCP (Default)**:
- Modern connection pooling
- Works well with virtual threads
- Configure max pool size

### Pattern with Virtual Threads

**Efficient Resource Usage**:
- Many virtual threads (thousands possible)
- Few database connections (20-30 typical)
- Virtual thread waits for connection when needed
- No wasted OS threads

## Error Handling

### Exception Propagation

**Traditional Try/Catch**:
- Works as expected on virtual threads
- Stack traces clear
- Exception handling normal

**Structured Concurrency**:
- Cancellation tokens
- Timeout handling
- Exception collection from tasks

## Testing with Virtual Threads

### Test Configuration

**Enable for Tests**:
- Property in test configuration
- Same as production
- Verify code works with virtual threads

### Test Patterns

**Concurrent Tests**:
- Launch multiple virtual threads
- Verify concurrent behavior
- Race condition detection

**Load Testing**:
- Thousands of concurrent operations
- Demonstrate scalability
- Stress test application

## Performance Considerations

### When Virtual Threads Excel

**I/O-Bound Applications**:
- Web servers handling many requests
- Microservices calling external APIs
- Database-heavy applications

**Memory Usage**:
- Virtual thread: ~1KB
- Platform thread: ~1MB
- Dramatic difference at scale

### When Virtual Threads Are Overkill

**Single Threaded**:
- Simple command-line tools
- Batch jobs with low concurrency
- Doesn't hurt, just unnecessary

**CPU-Bound**:
- Heavy computations
- Video encoding
- Use ForkJoinPool for CPU work

## Best Practices

1. **Let Spring Configure**: Use default virtual thread executor
2. **Write Synchronous Code**: Don't use callbacks/futures unnecessarily
3. **Respect Resource Limits**: Connection pools, database, external APIs
4. **Avoid ThreadLocal**: Use ScopedValue or context propagation
5. **Test Concurrency**: Verify behavior under load
6. **Monitor Threads**: Track virtual thread metrics
7. **I/O Patterns**: RestClient, JPA, file I/O all work perfectly
