# Testing Guide

Testing is a core part of TDD development. This guide covers conventions for writing effective tests in Java/Spring projects.

## Test Class Structure

**Naming Convention**:
- Production class: `Order`
- Test class: `OrderTest`
- Location: `src/test/java/...`

**Annotation**:
- Use `@SpringBootTest` for integration tests
- Use standard JUnit 5 for unit tests

## Test Method Naming

**Convention**: Describe the behavior being tested

**Pattern 1: shouldXxxWhenYyy**
```java
/**
 * 주문의 최종 금액을 올바르게 계산하는지 확인합니다.
 */
@Test
public void shouldCalculateFinalPriceWithDiscount() {
    // Arrange, Act, Assert
}
```

**Pattern 2: xxxWhenYyy**
```java
/**
 * 유효하지 않은 할인액으로 예외가 발생하는지 확인합니다.
 */
@Test
public void throwExceptionWhenDiscountExceedsTotal() {
    // Arrange, Act, Assert
}
```

## Test Structure (Given-When-Then Pattern)

All tests follow Given-When-Then pattern with clear sections:

```java
/**
 * 할인이 적용된 주문의 최종 금액을 올바르게 계산하는지 확인합니다.
 */
@Test
public void shouldCalculateTotalWithDiscount() {
    // Given: 3000원 총액의 주문
    Order order = new Order(1L, 3000);
    order.applyDiscount(500);

    // When: 최종 금액을 계산할 때
    long result = order.calculateTotal();

    // Then: 2500원이 반환되어야 함
    assertEquals(2500, result);
}
```

## Assertion Methods

**Basic Assertions**:
- `assertEquals(expected, actual)`: Check equality
- `assertTrue(condition)`: Verify true condition
- `assertFalse(condition)`: Verify false condition
- `assertNull(value)`: Check null value
- `assertNotNull(value)`: Check non-null value

**Exception Testing**:
```java
/**
 * 할인액이 총액을 초과하면 IllegalArgumentException이 발생합니다.
 */
@Test
public void throwExceptionWhenDiscountExceedsTotal() {
    // Given: 1000원 총액의 주문
    Order order = new Order(1L, 1000);

    // When & Then: 1500원을 할인하려 하면 예외가 발생해야 함
    assertThrows(
        IllegalArgumentException.class,
        () -> order.applyDiscount(1500)
    );
}
```

**Multiple Assertions**:
```java
/**
 * 주문 상태가 PENDING에서 CONFIRMED로 올바르게 전이됩니다.
 */
@Test
public void shouldTransitionOrderStatusCorrectly() {
    // Given: PENDING 상태의 주문
    Order order = new Order(1L, 2000);

    // When: confirm 메서드를 호출할 때
    order.confirm();

    // Then: 상태가 CONFIRMED로 변경되고 updatedAt이 갱신됨
    assertTrue(order.isStatus(OrderStatus.CONFIRMED.getValue()));
    assertNotNull(order.getUpdatedAt());
}
```

## Unit Tests vs Integration Tests

**Unit Test** (Fast, isolated):
```java
/**
 * Entity의 도메인 로직을 독립적으로 테스트합니다.
 * Repository나 Service와 무관하게 비즈니스 규칙을 검증합니다.
 */
public class OrderTest {
    /**
     * 할인이 적용된 최종 금액을 올바르게 계산합니다.
     */
    @Test
    public void shouldCalculateFinalPriceWithDiscount() {
        // Given: 3000원 총액, 500원 할인의 주문
        Order order = new Order(1L, 3000);
        order.applyDiscount(500);

        // When: 최종 금액을 계산할 때
        long result = order.calculateTotal();

        // Then: 2500원이 반환됨
        assertEquals(2500, result);
    }
}
```

**Integration Test** (Slower, full context):
```java
/**
 * Service와 Repository의 통합 동작을 테스트합니다.
 * 데이터베이스와의 실제 상호작용을 포함합니다.
 */
@SpringBootTest
public class OrderServiceIntegrationTest {
    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    /**
     * 주문 생성 요청부터 데이터베이스 저장까지의 전체 흐름이 정상 동작합니다.
     */
    @Test
    @Transactional
    public void shouldCreateAndPersistOrder() {
        // Given: 주문 생성 요청 데이터
        CreateOrderRequest request = new CreateOrderRequest(
            List.of(1L, 2L),
            5,
            "빠른 배송"
        );

        // When: 주문을 생성할 때
        OrderDto result = orderService.createOrder(request);

        // Then: 주문이 생성되고 데이터베이스에 저장됨
        assertNotNull(result.orderId());
        assertTrue(orderRepository.existsById(result.orderId()));
    }
}
```

## Test Data Setup

**Using Constructors**:
```java
Order order = new Order(1L, 3000);
```

**Using Builders** (if needed):
```java
Order order = Order.builder()
    .userId(1L)
    .totalPrice(3000)
    .status(OrderStatus.PENDING.getValue())
    .build();
```

**Using Test Fixtures**:
```java
private Order createTestOrder() {
    return new Order(1L, 3000);
}

@Test
public void shouldCalculateTotal() {
    Order order = createTestOrder();
    // Test logic
}
```

## Testing Service Layer

**Service tests use mocked repositories**:
```java
/**
 * Service 계층의 비즈니스 로직을 테스트합니다.
 * Repository는 mock으로 처리합니다.
 */
public class OrderServiceTest {
    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderService orderService;

    /**
     * 주문 조회 시 올바른 Repository 메서드를 호출하는지 확인합니다.
     */
    @Test
    public void shouldFetchOrderFromRepository() {
        // Arrange
        Order expectedOrder = new Order(1L, 2000);
        when(orderRepository.findById(1L))
            .thenReturn(Optional.of(expectedOrder));

        // Act
        OrderDto result = orderService.getOrder(1L);

        // Assert
        assertEquals(expectedOrder.getId(), result.orderId());
        verify(orderRepository, times(1)).findById(1L);
    }
}
```

## Testing Entity Domain Logic

**Focus on business rules**:
```java
/**
 * Entity의 도메인 로직을 테스트합니다.
 * 모든 비즈니스 규칙과 상태 전이가 올바르게 동작하는지 확인합니다.
 */
public class OrderTest {
    /**
     * 할인이 적용된 최종 금액을 올바르게 계산합니다.
     */
    @Test
    public void shouldCalculateFinalPriceWithDiscount() {
        // Given: 5000원 총액, 1000원 할인의 주문
        Order order = new Order(1L, 5000);
        order.applyDiscount(1000);

        // When: 최종 금액을 계산할 때
        long result = order.calculateTotal();

        // Then: 4000원이 반환됨
        assertEquals(4000, result);
    }

    /**
     * 주문 상태 전이 규칙을 정확하게 따릅니다.
     */
    @Test
    public void shouldEnforceOrderStatusTransitions() {
        // Given: PENDING 상태의 주문
        Order order = new Order(1L, 2000);

        // When & Then: PENDING에서 CONFIRMED로 전이 가능
        order.confirm();
        assertTrue(order.isStatus(OrderStatus.CONFIRMED.getValue()));

        // When & Then: CONFIRMED 상태에서 다시 confirm 호출 시 예외 발생
        assertThrows(
            IllegalStateException.class,
            order::confirm
        );
    }
}
```

## Test Execution

**Run all tests**:
```bash
./gradlew test
```

**Run specific test class**:
```bash
./gradlew test --tests OrderTest
```

**Run specific test method**:
```bash
./gradlew test --tests OrderTest.shouldCalculateTotal
```

## Best Practices

1. **One assertion per concept**: Multiple assertions OK if testing single behavior
2. **Clear test names**: Name describes what is being tested
3. **Fast unit tests**: Should complete in milliseconds
4. **Isolated tests**: No test should depend on another test
5. **Deterministic**: Tests produce same result every run
6. **No Test Order Dependency**: Tests should run in any order
7. **Mock external dependencies**: Don't call real APIs/databases in unit tests
8. **Setup before each test**: Use `@BeforeEach` for test preparation
9. **Clean up after test**: Use `@AfterEach` for cleanup if needed
10. **Meaningful assertions**: Assert on final state, not implementation details
